package xyz.sevive.arcaeaoffline.core.api

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class RemoteResourcesInfoUiState(
    val isFetching: Boolean = false,
    val info: ArcaeaResourcesRemoteInfo? = null,
    /** Failure text of the latest refresh; null after a successful refresh. */
    val errorText: String? = null,
)

class RemoteResourcesInfoStateHolder(
    private val resourcesApiClient: ArcaeaResourcesApiClient,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
    /** Emissions (e.g. base URL changes) trigger a refresh; the initial emission must be dropped upstream. */
    baseUrlChanges: Flow<*> = emptyFlow<Nothing>(),
) {
    private val logger = Logger.withTag(LOG_TAG)

    private val _state = MutableStateFlow(RemoteResourcesInfoUiState())
    val state: StateFlow<RemoteResourcesInfoUiState> = _state.asStateFlow()

    // All state transitions (fetch guard, publish, base URL reset, rerun bookkeeping) are serialized
    // by this mutex; the network fetch itself runs outside it. Without it, a refresh racing the
    // collector's reset could interleave and leave isFetching stuck on true with nothing running.
    // rerunPending and baseUrlGeneration are only touched while holding the lock.
    private val mutex = Mutex()
    private var rerunPending = false

    // Bumped on every base URL change: a probe started against the previous URL must not publish its result.
    private var baseUrlGeneration = 0

    init {
        // fetch latest data once constructed
        // this requires [RemoteResourcesInfoUiState.isFetching] defaulting to false,
        // otherwise the guard below will block this request
        refresh()
        scope.launch {
            baseUrlChanges.collect {
                mutex.withLock {
                    // Info probed against the previous URL would misreport the new one: drop it before re-probing.
                    baseUrlGeneration++
                    _state.value = RemoteResourcesInfoUiState(isFetching = _state.value.isFetching)

                    // A refresh in flight probes the previous URL; queue a rerun instead of
                    // dropping the change on the isFetching guard.
                    if (_state.value.isFetching) {
                        rerunPending = true
                    }
                }
                // While a fetch is in flight this is dropped by the guard and the queued
                // rerunPending covers the change instead.
                refresh()
            }
        }
    }

    fun refresh() {
        scope.launch { startRefresh() }
    }

    private suspend fun startRefresh() {
        // The generation is captured in the same critical section that raises the guard, so a base
        // URL change racing in between still invalidates this probe.
        val generation =
            mutex.withLock {
                if (_state.value.isFetching) {
                    null
                } else {
                    _state.value = _state.value.copy(isFetching = true)
                    baseUrlGeneration
                }
            } ?: return

        var result: ArcaeaResourcesRemoteInfo? = null
        var failureText: String? = null
        try {
            result = resourcesApiClient.fetchRemoteInfo()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.e(e) { "Error refreshing remote resources info" }
            failureText = throwableToErrorText(e)
        }

        var rerun = false
        mutex.withLock {
            if (generation == baseUrlGeneration) {
                _state.value =
                    if (result != null) {
                        RemoteResourcesInfoUiState(isFetching = false, info = result)
                    } else {
                        // Keep the previously fetched info: a transient failure must not wipe known metadata.
                        _state.value.copy(isFetching = false, errorText = failureText)
                    }
            } else {
                // The collector already cleared the state; just release the guard so the rerun can start.
                _state.value = _state.value.copy(isFetching = false)
            }

            if (rerunPending) {
                rerunPending = false
                rerun = true
            }
        }
        if (rerun) refresh()
    }

    companion object {
        private const val LOG_TAG = "RemoteResourcesInfo"
    }
}
