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

    init {
        // fetch latest data once constructed
        // this requires [RemoteResourcesInfoUiState.isFetching] defaulting to false,
        // otherwise the guard below will block this request
        refresh()
        scope.launch {
            baseUrlChanges.collect {
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
        scope.launch {
            try {
                val info = resourcesApiClient.fetchRemoteInfo()
                _state.value = RemoteResourcesInfoUiState(isFetching = false, info = info)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logger.e(e) { "Error refreshing remote resources info" }
                // Keep the previously fetched info: a transient failure must not wipe known metadata.
                _state.value = _state.value.copy(isFetching = false, errorText = throwableToErrorText(e))
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
