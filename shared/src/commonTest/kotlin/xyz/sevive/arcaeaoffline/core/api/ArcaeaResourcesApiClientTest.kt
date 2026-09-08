package xyz.sevive.arcaeaoffline.core.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respondError
import io.ktor.client.engine.mock.respondOk
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Instant

class ArcaeaResourcesApiClientTest {
    // Example index.json response (2026-09-07)
    private val indexJson =
        """{"latest": "7.0.255", "versions": [{"version": "7.0.255", "built_at": "2026-09-06T23:46:11+00:00"}]}"""

    private val expectedBuiltAt = Instant.parse("2026-09-06T23:46:11+00:00").toEpochMilliseconds()

    private val httpClients = mutableListOf<HttpClient>()

    private fun client(
        baseUrl: String = "https://example.test/publish",
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
    ): ArcaeaResourcesApiClient {
        val httpClient = HttpClient(MockEngine { handler(it) })
        httpClients.add(httpClient)
        return ArcaeaResourcesApiClient(
            baseUrlFlow = MutableStateFlow(baseUrl),
            httpClient = httpClient,
        )
    }

    @AfterTest
    fun tearDown() {
        httpClients.forEach { it.close() }
        httpClients.clear()
    }

    @Test
    fun fetchRemoteInfoReturnsAllFilesWhenAvailable() =
        runTest {
            val client =
                client { request ->
                    if (request.url.toString().endsWith("index.json")) respondOk(indexJson) else respondOk("")
                }

            val info = client.fetchRemoteInfo()

            for (file in listOf(info.packlist, info.songlist, info.chartInfoDatabase, info.imageHashesDatabase)) {
                assertEquals(true, file.isAvailable, file.errorText)
                assertEquals("7.0.255", file.version)
                assertEquals(expectedBuiltAt, file.builtAt)
                assertEquals(null, file.errorText)
            }
        }

    @Test
    fun fetchRemoteInfoReportsIndexFailureOnEveryFile() =
        runTest {
            val client = client { throw java.io.IOException("network down") }

            val info = client.fetchRemoteInfo()

            for (file in listOf(info.packlist, info.songlist, info.chartInfoDatabase, info.imageHashesDatabase)) {
                assertEquals(false, file.isAvailable)
                assertEquals(null, file.version)
                assertEquals(null, file.builtAt)
                assertEquals(throwableToErrorText(java.io.IOException("network down")), file.errorText)
            }
        }

    @Test
    fun fetchRemoteInfoIsolatesSingleFileFailure() =
        runTest {
            val client =
                client { request ->
                    when {
                        request.url.toString().endsWith("index.json") -> respondOk(indexJson)
                        request.url.toString().endsWith("packlist") -> respondError(HttpStatusCode.NotFound)
                        request.url.toString().endsWith("songlist") -> throw java.io.IOException("timeout")
                        else -> respondOk("")
                    }
                }

            val info = client.fetchRemoteInfo()

            assertEquals(false, info.packlist.isAvailable)
            assertEquals("404", info.packlist.errorText)
            assertEquals(false, info.songlist.isAvailable)
            assertContains(info.songlist.errorText!!, "IOException")
            for (file in listOf(info.chartInfoDatabase, info.imageHashesDatabase)) {
                assertEquals(true, file.isAvailable)
            }
        }

    @Test
    fun fetchRemoteInfoHandlesMissingBuiltAt() =
        runTest {
            val client =
                client { request ->
                    if (request.url.toString().endsWith("index.json")) {
                        respondOk("""{"latest": "7.0.255", "versions": [{"version": "7.0.255"}]}""")
                    } else {
                        respondOk("")
                    }
                }

            val info = client.fetchRemoteInfo()

            for (file in listOf(info.packlist, info.songlist, info.chartInfoDatabase, info.imageHashesDatabase)) {
                assertEquals(null, file.builtAt)
            }
        }

    @Test
    fun publishUrlNormalizesBaseUrl() =
        runTest {
            val urls = mutableListOf<String>()
            val client =
                client("  https://example.test/publish/  ") { request ->
                    urls.add(request.url.toString())
                    if (request.url.toString().endsWith("index.json")) respondOk(indexJson) else respondOk("")
                }

            client.fetchRemoteInfo()

            assertEquals(
                listOf(
                    "https://example.test/publish/index.json",
                    "https://example.test/publish/7.0.255/packlist",
                    "https://example.test/publish/7.0.255/songlist",
                    "https://example.test/publish/7.0.255/ci.db",
                    "https://example.test/publish/7.0.255/ih.db",
                ),
                urls,
            )
        }

    @Test
    fun publishTextThrowsApiExceptionOnHttpError() =
        runTest {
            val client =
                client { request ->
                    when {
                        request.url.toString().endsWith("index.json") -> respondOk(indexJson)
                        request.url.toString().endsWith("packlist") -> respondError(HttpStatusCode.InternalServerError)
                        else -> respondOk("")
                    }
                }

            val e = assertFailsWith<ArcaeaResourcesApiException> { client.packlist() }

            assertContains(e.message!!, "500")
        }

    @Test
    fun parseBuiltAtAcceptsIsoAndRejectsGarbage() {
        assertEquals(expectedBuiltAt, parseBuiltAt("2026-09-06T23:46:11+00:00"))
        assertEquals(null, parseBuiltAt("not a date"))
    }
}
