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

    // Written by the baseUrlChanges collector and read by refresh coroutines, both on the IO pool.
    @Volatile
    private var rerunPending = false

    // Bumped on every base URL change: a probe started against the previous URL must not publish its result.
    @Volatile
    private var baseUrlGeneration = 0

    init {
        // fetch latest data once constructed
        // this requires [RemoteResourcesInfoUiState.isFetching] defaulting to false,
        // otherwise the guard below will block this request
        refresh()
        scope.launch {
            baseUrlChanges.collect {
                // Info probed against the previous URL would misreport the new one: drop it before re-probing.
                baseUrlGeneration++
                _state.value = RemoteResourcesInfoUiState(isFetching = _state.value.isFetching)

                // A refresh in flight probes the previous URL; queue a rerun instead of
                // dropping the change on the isFetching guard.
                if (_state.value.isFetching) {
                    rerunPending = true
                } else {
                    refresh()
                }
            }
        }
    }

    fun refresh() {
        if (_state.value.isFetching) return

        _state.value = _state.value.copy(isFetching = true)
        val generation = baseUrlGeneration
        scope.launch {
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
                refresh()
            }
        }
    }

    companion object {
        private const val LOG_TAG = "RemoteResourcesInfo"
    }
}
