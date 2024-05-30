package xyz.sevive.arcaeaoffline.ui.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import xyz.sevive.arcaeaoffline.ui.containers.ArcaeaOfflineDatabaseRepositoryContainer

class OverviewViewModel(
    private val repositoryContainer: ArcaeaOfflineDatabaseRepositoryContainer
) : ViewModel() {
    data class UiState(
        val isLoading: Boolean = true,
        val b30: Double? = null,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    fun reload() {
        viewModelScope.launch {
            _uiState.value = UiState()
            val b30 = repositoryContainer.b30()
            _uiState.value = UiState(isLoading = false, b30 = b30)
        }
    }
}
