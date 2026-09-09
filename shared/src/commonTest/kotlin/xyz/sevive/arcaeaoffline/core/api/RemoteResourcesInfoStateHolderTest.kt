package xyz.sevive.arcaeaoffline.core.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respondOk
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class RemoteResourcesInfoStateHolderTest {
    // Example index.json response (2026-09-07)
    private val indexJson =
        """{"latest": "7.0.255", "versions": [{"version": "7.0.255", "built_at": "2026-09-06T23:46:11+00:00"}]}"""

    private var handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData =
        { respondOk("") }

    private val clients = mutableListOf<HttpClient>()
    private val scopeJobs = mutableListOf<Job>()

    // The engine defaults to a real IO dispatcher; pin it to the test scheduler so the tests stay
    // single-threaded and deterministic.
    private val testDispatcher = StandardTestDispatcher()

    private fun engine() =
        MockEngine(
            MockEngineConfig().apply {
                dispatcher = testDispatcher
                addHandler { request -> handler(request) }
            },
        )

    private fun client(baseUrlFlow: Flow<String> = MutableStateFlow("https://example.test/publish")) =
        ArcaeaResourcesApiClient(
            baseUrlFlow = baseUrlFlow,
            httpClient = HttpClient(engine()).also { clients.add(it) },
        )

    // A plain scope, not backgroundScope: advanceUntilIdle stops while only background-scope tasks
    // remain, which would leave the init refresh unfinished.
    private fun holderScope(): CoroutineScope {
        val job = SupervisorJob()
        scopeJobs += job
        return CoroutineScope(job + testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        clients.forEach { it.close() }
        clients.clear()
        scopeJobs.forEach { it.cancel() }
        scopeJobs.clear()
    }

    @Test
    fun successfulInitRefreshPopulatesState() =
        runTest(testDispatcher) {
            handler = { request ->
                if (request.url.toString().endsWith("index.json")) respondOk(indexJson) else respondOk("")
            }
            val holder = RemoteResourcesInfoStateHolder(client(), holderScope())
            advanceUntilIdle()

            val state = holder.state.value

            assertFalse(state.isFetching)
            assertNull(state.errorText)
            assertNotNull(state.info)
        }

    @Test
    fun refreshWithFailingIndexSurfacesPerFileErrorText() =
        runTest(testDispatcher) {
            var fail = false
            handler = { request ->
                if (fail) throw java.io.IOException("network down")
                if (request.url.toString().endsWith("index.json")) respondOk(indexJson) else respondOk("")
            }
            val holder = RemoteResourcesInfoStateHolder(client(), holderScope())
            advanceUntilIdle()
            assertNotNull(holder.state.value.info)

            fail = true
            holder.refresh()
            advanceUntilIdle()
            val state = holder.state.value

            assertFalse(state.isFetching)
            // An index failure degrades the info (per-file error text) instead of throwing to the holder.
            assertFalse(state.info!!.packlist.isAvailable)
            assertTrue(
                state.info
                    .packlist.errorText!!
                    .contains("IOException"),
            )
            assertNull(state.errorText)
        }

    @Test
    fun refreshWhileFetchingIsIgnored() =
        runTest(testDispatcher) {
            val gate = CompletableDeferred<Unit>()
            var indexRequests = 0
            handler = { request ->
                if (request.url.toString().endsWith("index.json")) {
                    indexRequests++
                    gate.await()
                    respondOk(indexJson)
                } else {
                    respondOk("")
                }
            }
            val holder = RemoteResourcesInfoStateHolder(client(), holderScope())
            // Run until the init refresh suspends on the gate.
            runCurrent()
            assertTrue(holder.state.value.isFetching)

            // Two overlapping calls: both must be dropped by the guard, not just the first one.
            holder.refresh()
            holder.refresh()
            gate.complete(Unit)
            advanceUntilIdle()

            assertEquals(1, indexRequests, "refresh calls during an in-flight refresh must be dropped")
            assertFalse(holder.state.value.isFetching)
        }

    @Test
    fun refreshFailureKeepsPreviousInfoAndSetsErrorText() =
        runTest(testDispatcher) {
            var fail = false
            val flakyClient =
                object : ArcaeaResourcesApiClient(
                    baseUrlFlow = MutableStateFlow("https://example.test/publish"),
                    httpClient = HttpClient(engine()).also { clients.add(it) },
                ) {
                    override suspend fun fetchRemoteInfo(): ArcaeaResourcesRemoteInfo {
                        if (fail) throw java.io.IOException("boom")
                        return ArcaeaResourcesRemoteInfo(
                            packlist = ArcaeaResourcesRemoteFileInfo(true, "7.0.255", 1L, null),
                            songlist = ArcaeaResourcesRemoteFileInfo(true, "7.0.255", 1L, null),
                            chartInfoDatabase = ArcaeaResourcesRemoteFileInfo(true, "7.0.255", 1L, null),
                            imageHashesDatabase = ArcaeaResourcesRemoteFileInfo(true, "7.0.255", 1L, null),
                        )
                    }
                }
            val holder = RemoteResourcesInfoStateHolder(flakyClient, holderScope())
            advanceUntilIdle()
            val knownInfo = assertNotNull(holder.state.value.info)

            fail = true
            holder.refresh()
            advanceUntilIdle()
            val state = holder.state.value

            assertFalse(state.isFetching)
            assertEquals(knownInfo, state.info)
            assertTrue(state.errorText!!.contains("IOException"))
        }

    @Test
    fun baseUrlChangeDuringRefreshRerunsAfterSettle() =
        runTest(testDispatcher) {
            val gate = CompletableDeferred<Unit>()
            val urls = mutableListOf<String>()
            val urlFlow = MutableStateFlow("https://example.test/publish")
            val changingClient =
                ArcaeaResourcesApiClient(
                    baseUrlFlow = urlFlow,
                    httpClient = HttpClient(engine()).also { clients.add(it) },
                )
            handler = { request ->
                urls.add(request.url.toString())
                if (request.url.toString().endsWith("index.json")) {
                    gate.await()
                    respondOk(indexJson)
                } else {
                    respondOk("")
                }
            }
            // The flow's initial emission must be dropped upstream; start reacting from the first change.
            val holder =
                RemoteResourcesInfoStateHolder(
                    changingClient,
                    holderScope(),
                    baseUrlChanges = urlFlow.drop(1),
                )
            // Run until the init refresh suspends on the gate.
            runCurrent()
            assertTrue(holder.state.value.isFetching)

            urlFlow.value = "https://other.example/publish"
            gate.complete(Unit)
            advanceUntilIdle()

            // The first refresh probed the old URL; the queued rerun must have probed the new one.
            val indexUrls = urls.filter { it.endsWith("index.json") }
            assertEquals(
                listOf("https://example.test/publish/index.json", "https://other.example/publish/index.json"),
                indexUrls,
            )
            assertFalse(holder.state.value.isFetching)
        }

    @Test
    fun baseUrlChangeClearsInfoWhenReprobeFails() =
        runTest(testDispatcher) {
            val urlFlow = MutableStateFlow("https://example.test/publish")
            handler = { request ->
                if (request.url.toString().contains("other.example")) throw java.io.IOException("new host down")
                if (request.url.toString().endsWith("index.json")) respondOk(indexJson) else respondOk("")
            }
            val holder =
                RemoteResourcesInfoStateHolder(
                    client(urlFlow),
                    holderScope(),
                    baseUrlChanges = urlFlow.drop(1),
                )
            advanceUntilIdle()
            assertNotNull(holder.state.value.info)

            urlFlow.value = "https://other.example/publish"
            advanceUntilIdle()
            val state = holder.state.value

            // The info object must describe the new URL: no version/built_at carried over from the old one.
            val info = assertNotNull(state.info)
            assertNull(info.packlist.version)
            assertFalse(info.packlist.isAvailable)
            assertTrue(info.packlist.errorText!!.contains("IOException"))
            assertFalse(state.isFetching)
        }
}
