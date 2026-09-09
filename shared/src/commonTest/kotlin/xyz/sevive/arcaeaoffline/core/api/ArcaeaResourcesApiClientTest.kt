package xyz.sevive.arcaeaoffline.core.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.engine.mock.respondOk
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.files.SystemTemporaryDirectory
import kotlinx.io.readByteArray
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.time.Instant

class ArcaeaResourcesApiClientTest {
    // Example index.json response (2026-09-07)
    private val indexJson =
        """{"latest": "7.0.255", "versions": [{"version": "7.0.255", "built_at": "2026-09-06T23:46:11+00:00"}]}"""

    private val expectedBuiltAt = Instant.parse("2026-09-06T23:46:11+00:00").toEpochMilliseconds()

    private val httpClients = mutableListOf<HttpClient>()

    private fun client(
        baseUrl: String = "https://example.test/publish",
        maxResourceBytes: Long = 20L * 1024 * 1024,
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
    ): ArcaeaResourcesApiClient {
        val httpClient = HttpClient(MockEngine { handler(it) })
        httpClients.add(httpClient)
        return ArcaeaResourcesApiClient(
            baseUrlFlow = MutableStateFlow(baseUrl),
            httpClient = httpClient,
            maxResourceBytes = maxResourceBytes,
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
                    when {
                        request.url.toString().endsWith("index.json") -> {
                            assertEquals(HttpMethod.Get, request.method)
                            respondOk(indexJson)
                        }

                        else -> {
                            // Probes must stay HEAD: probing with GET would pull the whole file just to test existence.
                            assertEquals(HttpMethod.Head, request.method)
                            respondOk("")
                        }
                    }
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
            val engine =
                MockEngine { request ->
                    if (request.url.toString().endsWith("index.json")) respondOk(indexJson) else respondOk("")
                }
            val httpClient = HttpClient(engine).also { httpClients.add(it) }
            val client = ArcaeaResourcesApiClient(MutableStateFlow("  https://example.test/publish/  "), httpClient)

            client.fetchRemoteInfo()

            // requestHistory is the engine's own thread-safe log: the four probes run concurrently,
            // so collecting URLs from the handlers would race and their order is not a contract.
            val urls = engine.requestHistory.map { it.url.toString() }
            assertEquals("https://example.test/publish/index.json", urls.first())
            assertEquals(
                setOf(
                    "https://example.test/publish/index.json",
                    "https://example.test/publish/7.0.255/packlist",
                    "https://example.test/publish/7.0.255/songlist",
                    "https://example.test/publish/7.0.255/ci.db",
                    "https://example.test/publish/7.0.255/ih.db",
                ),
                urls.toSet(),
            )
            assertEquals(5, urls.size)
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

    @Test
    fun fetchRemoteInfoHandlesLatestMissingFromVersions() =
        runTest {
            val client =
                client { request ->
                    if (request.url.toString().endsWith("index.json")) {
                        respondOk(
                            """{"latest": "7.0.255", "versions": [{"version": "7.0.0", "built_at": "2026-01-01T00:00:00+00:00"}]}""",
                        )
                    } else {
                        respondOk("")
                    }
                }

            val info = client.fetchRemoteInfo()

            for (file in listOf(info.packlist, info.songlist, info.chartInfoDatabase, info.imageHashesDatabase)) {
                assertEquals("7.0.255", file.version)
                assertEquals(null, file.builtAt)
            }
        }

    @Test
    fun fetchRemoteInfoPicksBuiltAtOfMatchingVersion() =
        runTest {
            val client =
                client { request ->
                    if (request.url.toString().endsWith("index.json")) {
                        respondOk(
                            """{"latest": "7.0.255", "versions": [""" +
                                """{"version": "7.0.0", "built_at": "2026-01-01T00:00:00+00:00"}, """ +
                                """{"version": "7.0.255", "built_at": "2026-09-06T23:46:11+00:00"}]}""",
                        )
                    } else {
                        respondOk("")
                    }
                }

            val info = client.fetchRemoteInfo()

            for (file in listOf(info.packlist, info.songlist, info.chartInfoDatabase, info.imageHashesDatabase)) {
                assertEquals("7.0.255", file.version)
                assertEquals(expectedBuiltAt, file.builtAt)
            }
        }

    @Test
    fun downloadWritesChunkedBodyToFile() =
        runTest {
            // Two full download chunks plus a tail, to pin down the copy loop's boundaries.
            val expected = ByteArray(2 * 64 * 1024 + 17) { (it % 251).toByte() }
            val client =
                client { request ->
                    when {
                        request.url.toString().endsWith("index.json") -> respondOk(indexJson)
                        request.url.toString().endsWith("ci.db") -> respond(expected)
                        else -> respondError(HttpStatusCode.NotFound)
                    }
                }

            val dir = Path(SystemTemporaryDirectory, "arcaea-resources-client-test")
            val dest = Path(dir, "ci.db")
            SystemFileSystem.createDirectories(dir)
            try {
                client.downloadChartInfoDatabase(dest)

                val actual = SystemFileSystem.source(dest).buffered().use { it.readByteArray() }
                assertEquals(expected.size, actual.size)
                assertTrue(expected.contentEquals(actual))
            } finally {
                SystemFileSystem.delete(dest, mustExist = false)
                SystemFileSystem.delete(dir, mustExist = false)
            }
        }

    @Test
    fun publishTextRejectsOversizedDeclaredBody() =
        runTest {
            // The declared length must match the body, or ktor rejects the response itself before
            // the client's own size check runs.
            val oversized = ByteArray(2048) { 'x'.code.toByte() }
            val client =
                client(maxResourceBytes = 1024) { request ->
                    when {
                        request.url.toString().endsWith("index.json") -> respondOk(indexJson)
                        else -> respond(oversized, HttpStatusCode.OK, headersOf(HttpHeaders.ContentLength, "2048"))
                    }
                }

            val e = assertFailsWith<IOException> { client.packlist() }

            assertContains(e.message!!, "exceeds")
        }

    @Test
    fun publishTextRejectsOversizedStreamedBody() =
        runTest {
            // No Content-Length on the response: the streamed byte count is the bound.
            val client =
                client(maxResourceBytes = 1024) { request ->
                    when {
                        request.url.toString().endsWith("index.json") -> respondOk(indexJson)
                        else -> respond(ByteArray(2048) { 'x'.code.toByte() })
                    }
                }

            val e = assertFailsWith<IOException> { client.packlist() }

            assertContains(e.message!!, "exceeds")
        }

    @Test
    fun downloadRejectsOversizedBody() =
        runTest {
            val client =
                client(maxResourceBytes = 1024) { request ->
                    when {
                        request.url.toString().endsWith("index.json") -> respondOk(indexJson)
                        request.url.toString().endsWith("ci.db") -> respond(ByteArray(2048) { 'x'.code.toByte() })
                        else -> respondError(HttpStatusCode.NotFound)
                    }
                }

            val dir = Path(SystemTemporaryDirectory, "arcaea-resources-client-test-oversize")
            val dest = Path(dir, "ci.db")
            SystemFileSystem.createDirectories(dir)
            try {
                assertFailsWith<IOException> { client.downloadChartInfoDatabase(dest) }
            } finally {
                SystemFileSystem.delete(dest, mustExist = false)
                SystemFileSystem.delete(dir, mustExist = false)
            }
        }

    @Test
    fun fetchRemoteInfoRejectsFileWhenHeadReportsOversize() =
        runTest {
            // The HEAD response carries no body; the client must reject on the declared
            // Content-Length alone, before any download is attempted.
            val client =
                client(maxResourceBytes = 1024) { request ->
                    when {
                        request.url.toString().endsWith("index.json") -> {
                            respondOk(indexJson)
                        }

                        request.url.toString().endsWith("packlist") -> {
                            respond("", HttpStatusCode.OK, headersOf(HttpHeaders.ContentLength, "2048"))
                        }

                        request.url.toString().endsWith("songlist") -> {
                            respond("", HttpStatusCode.OK, headersOf(HttpHeaders.ContentLength, "1024"))
                        }

                        else -> {
                            respondOk("")
                        }
                    }
                }

            val info = client.fetchRemoteInfo()

            assertEquals(false, info.packlist.isAvailable)
            assertContains(info.packlist.errorText!!, "exceeds")
            // Content-Length exactly at the limit is still available.
            assertEquals(true, info.songlist.isAvailable)
            assertEquals(true, info.chartInfoDatabase.isAvailable)
        }
}
