package xyz.sevive.arcaeaoffline.core.api

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RemoteResourcesInfoUiState(
    val isFetching: Boolean = false,
    val info: ArcaeaResourcesRemoteInfo? = null,
    /** Failure text of the latest refresh; null after a successful refresh. */
    val errorText: String? = null,
)

class RemoteResourcesInfoStateHolder(
    private val resourcesApiClient: ArcaeaResourcesApiClient,
) {
    private val logger = Logger.withTag(LOG_TAG)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _state = MutableStateFlow(RemoteResourcesInfoUiState())
    val state: StateFlow<RemoteResourcesInfoUiState> = _state.asStateFlow()

    init {
        // fetch latest data once constructed
        // this requires [RemoteResourcesInfoUiState.isFetching] defaulting to false,
        // otherwise the guard below will block this request
        refresh()
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
        }
    }

    companion object {
        private const val LOG_TAG = "RemoteResourcesInfo"
    }
}
