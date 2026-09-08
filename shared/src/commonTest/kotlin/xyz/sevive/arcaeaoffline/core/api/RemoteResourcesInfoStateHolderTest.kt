package xyz.sevive.arcaeaoffline.core.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respondOk
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RemoteResourcesInfoStateHolderTest {
    // Example index.json response (2026-09-07)
    private val indexJson =
        """{"latest": "7.0.255", "versions": [{"version": "7.0.255", "built_at": "2026-09-06T23:46:11+00:00"}]}"""

    // The ktor engine executes requests on its own dispatcher; both fields are
    // written by the test thread and read by the engine thread.
    @Volatile
    private var handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData =
        { respondOk("") }

    @Volatile
    private var fail = false

    private val clients = mutableListOf<HttpClient>()

    private fun client() =
        ArcaeaResourcesApiClient(
            baseUrlFlow = MutableStateFlow("https://example.test/publish"),
            httpClient = HttpClient(MockEngine { handler(it) }).also { clients.add(it) },
        )

    @AfterTest
    fun tearDown() {
        clients.forEach { it.close() }
        clients.clear()
    }

    @Test
    fun successfulInitRefreshPopulatesState() =
        runTest {
            handler = { request ->
                if (request.url.toString().endsWith("index.json")) respondOk(indexJson) else respondOk("")
            }
            val holder = RemoteResourcesInfoStateHolder(client())

            // The refresh runs on the engine's dispatcher; wait for the outcome instead of asserting timing.
            val state = holder.state.first { it.info != null || it.errorText != null }

            assertFalse(state.isFetching)
            assertNull(state.errorText)
            assertNotNull(state.info)
        }

    @Test
    fun refreshWithFailingIndexSurfacesPerFileErrorText() =
        runTest {
            handler = { request ->
                if (fail) throw java.io.IOException("network down")
                if (request.url.toString().endsWith("index.json")) respondOk(indexJson) else respondOk("")
            }
            val holder = RemoteResourcesInfoStateHolder(client())
            holder.state.first { it.info != null }

            fail = true
            holder.refresh()
            val state = holder.state.first { it.info?.packlist?.errorText != null }

            assertFalse(state.isFetching)
            // An index failure degrades the info (per-file error text) instead of throwing to the holder.
            assertEquals(false, state.info!!.packlist.isAvailable)
            assertTrue(
                state.info
                    .packlist.errorText!!
                    .contains("IOException"),
            )
            assertEquals(null, state.errorText)
        }

    @Test
    fun refreshWhileFetchingIsIgnored() =
        runTest {
            val gate = CompletableDeferred<Unit>()
            val indexRequests = AtomicInteger()
            handler = { request ->
                if (request.url.toString().endsWith("index.json")) {
                    indexRequests.incrementAndGet()
                    gate.await()
                    respondOk(indexJson)
                } else {
                    respondOk("")
                }
            }
            val holder = RemoteResourcesInfoStateHolder(client())
            // Wait until the init refresh is provably in flight.
            holder.state.first { it.isFetching }

            holder.refresh()
            gate.complete(Unit)
            val state = holder.state.first { it.info != null }

            assertEquals(1, indexRequests.get(), "a refresh call during an in-flight refresh must be dropped")
            assertFalse(state.isFetching)
        }
}
