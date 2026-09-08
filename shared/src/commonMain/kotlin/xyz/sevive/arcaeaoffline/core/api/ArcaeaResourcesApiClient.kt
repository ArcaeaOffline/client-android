package xyz.sevive.arcaeaoffline.core.api

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.head
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readRemaining
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.time.Instant

class ArcaeaResourcesApiException(
    val status: HttpStatusCode,
    url: String,
) : RuntimeException("Request to $url failed with status $status")

@Serializable
internal data class RemoteIndexDto(
    @SerialName("latest") val latest: String,
    @SerialName("versions") val versions: List<RemoteIndexVersionDto> = emptyList(),
)

@Serializable
internal data class RemoteIndexVersionDto(
    @SerialName("version") val version: String,
    @SerialName("built_at") val builtAt: String? = null,
)

private val json = Json { ignoreUnknownKeys = true }

/** Raw error text for display: exception class name + message, if any. */
fun throwableToErrorText(e: Throwable): String =
    buildString {
        append(e::class.simpleName ?: "Exception")
        e.message?.let { append(": ").append(it) }
    }

internal fun parseRemoteIndex(text: String): RemoteIndexDto = json.decodeFromString<RemoteIndexDto>(text)

internal fun parseBuiltAt(text: String): Long? = runCatching { Instant.parse(text).toEpochMilliseconds() }.getOrNull()

/** Probe result of a single remote file. */
data class ArcaeaResourcesRemoteFileInfo(
    val isAvailable: Boolean,
    /** Version from index.json's latest; null if the index could not be fetched. */
    val version: String?,
    /** built_at of that version from index.json, epoch milliseconds; null if missing or unparsable. */
    val builtAt: Long?,
    /** Reason when isAvailable = false: an HTTP status code, or an exception class name with message. */
    val errorText: String?,
)

/** Remote metadata of the publish directory. */
data class ArcaeaResourcesRemoteInfo(
    val packlist: ArcaeaResourcesRemoteFileInfo,
    val songlist: ArcaeaResourcesRemoteFileInfo,
    val chartInfoDatabase: ArcaeaResourcesRemoteFileInfo,
    val imageHashesDatabase: ArcaeaResourcesRemoteFileInfo,
) {
    operator fun get(resource: DownloadableResource): ArcaeaResourcesRemoteFileInfo =
        when (resource) {
            DownloadableResource.PACKLIST -> packlist
            DownloadableResource.SONGLIST -> songlist
            DownloadableResource.CHART_INFO_DATABASE -> chartInfoDatabase
            DownloadableResource.IMAGE_HASHES_DATABASE -> imageHashesDatabase
        }
}

class ArcaeaResourcesApiClient(
    private val baseUrlFlow: Flow<String>,
    private val httpClient: HttpClient = platformHttpClient(),
) {
    companion object {
        const val DEFAULT_BASE_URL = "https://arcaeaoffline.sevive.xyz/publish"
        private const val INDEX_FILE_NAME = "index.json"
        private const val DOWNLOAD_CHUNK_SIZE = 64 * 1024
    }

    suspend fun packlist(): String = publishText(versionedPath(DownloadableResource.PACKLIST))

    suspend fun songlist(): String = publishText(versionedPath(DownloadableResource.SONGLIST))

    /** Fetches the index first, then probes every published file in parallel; one file's failure does not affect the others. */
    suspend fun fetchRemoteInfo(): ArcaeaResourcesRemoteInfo =
        coroutineScope {
            val (index, indexErrorText) =
                try {
                    fetchIndex() to null
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    null to throwableToErrorText(e)
                }

            val latest = index?.latest
            val builtAt = index?.builtAt

            val packlist = async { fetchFileInfo(DownloadableResource.PACKLIST, latest, builtAt, indexErrorText) }
            val songlist = async { fetchFileInfo(DownloadableResource.SONGLIST, latest, builtAt, indexErrorText) }
            val chartInfoDatabase = async { fetchFileInfo(DownloadableResource.CHART_INFO_DATABASE, latest, builtAt, indexErrorText) }
            val imageHashesDatabase = async { fetchFileInfo(DownloadableResource.IMAGE_HASHES_DATABASE, latest, builtAt, indexErrorText) }

            ArcaeaResourcesRemoteInfo(
                packlist = packlist.await(),
                songlist = songlist.await(),
                chartInfoDatabase = chartInfoDatabase.await(),
                imageHashesDatabase = imageHashesDatabase.await(),
            )
        }

    /** Streams ci.db to [dest] without content validation; validation is up to the caller's import flow. */
    suspend fun downloadChartInfoDatabase(dest: Path) = downloadFile(DownloadableResource.CHART_INFO_DATABASE, dest)

    /** Streams ih.db to [dest] without content validation; validation is up to the caller's import flow. */
    suspend fun downloadImageHashesDatabase(dest: Path) = downloadFile(DownloadableResource.IMAGE_HASHES_DATABASE, dest)

    private data class ResolvedIndex(
        val latest: String,
        val builtAt: Long?,
    )

    /** Versioned path segment: fetch the index for latest, then compose "{latest}/{resource.fileName}". */
    private suspend fun versionedPath(resource: DownloadableResource): String {
        val index = fetchIndex()
        return "${index.latest}/${resource.fileName}"
    }

    private suspend fun fetchIndex(): ResolvedIndex {
        val dto = parseRemoteIndex(publishText(INDEX_FILE_NAME))
        val builtAt =
            dto.versions
                .firstOrNull { it.version == dto.latest }
                ?.builtAt
                ?.let(::parseBuiltAt)
        return ResolvedIndex(dto.latest, builtAt)
    }

    private suspend fun publishText(path: String): String {
        val url = publishUrl(path)
        val response = httpClient.get(url)
        response.checkStatus(url)
        return response.bodyAsText()
    }

    /** HEAD-probes a file; network failures count as unavailable with the reason recorded, never thrown. */
    private suspend fun fetchFileInfo(
        resource: DownloadableResource,
        version: String?,
        builtAt: Long?,
        indexErrorText: String?,
    ): ArcaeaResourcesRemoteFileInfo {
        if (version == null) {
            // Index fetch failed: the versioned path is unknown, so report the index error for every file.
            return ArcaeaResourcesRemoteFileInfo(false, null, null, indexErrorText)
        }

        val isAvailableAndErrorText =
            try {
                val url = publishUrl("$version/${resource.fileName}")
                val response = httpClient.head(url)

                if (response.status.isSuccess()) {
                    true to null
                } else {
                    false to response.status.value.toString()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                false to throwableToErrorText(e)
            }

        return ArcaeaResourcesRemoteFileInfo(
            isAvailable = isAvailableAndErrorText.first,
            version = version,
            builtAt = builtAt,
            errorText = isAvailableAndErrorText.second,
        )
    }

    private suspend fun publishUrl(fileName: String): String {
        val baseUrl = baseUrlFlow.first().trim().trimEnd('/')
        return "$baseUrl/$fileName"
    }

    private fun HttpResponse.checkStatus(url: String) {
        if (!status.isSuccess()) throw ArcaeaResourcesApiException(status, url)
    }

    private suspend fun downloadFile(
        resource: DownloadableResource,
        dest: Path,
    ) {
        val url = publishUrl(versionedPath(resource))
        httpClient.prepareGet(url).execute { response ->
            response.checkStatus(url)
            copyBodyToFile(response.bodyAsChannel(), dest)
        }
    }

    private suspend fun copyBodyToFile(
        channel: ByteReadChannel,
        dest: Path,
    ) {
        SystemFileSystem.sink(dest).buffered().use { sink ->
            val buffer = ByteArray(DOWNLOAD_CHUNK_SIZE)
            while (!channel.isClosedForRead) {
                val chunk = channel.readRemaining(DOWNLOAD_CHUNK_SIZE.toLong())
                while (!chunk.exhausted()) {
                    val read = chunk.readAtMostTo(buffer)
                    sink.write(buffer, 0, read)
                }
            }
        }
    }
}
