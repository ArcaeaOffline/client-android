package xyz.sevive.arcaeaoffline.ui.screens.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import xyz.sevive.arcaeaoffline.core.database.repositories.PotentialRepository

class OverviewViewModel(private val potentialRepository: PotentialRepository) : ViewModel() {
    data class UiState(
        val isLoading: Boolean = true,
        val b30: Double? = null,
        val r10: Double? = null,
        val potential: Double? = null,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    fun reload() {
        viewModelScope.launch {
            _uiState.value = UiState()
            val b30 = potentialRepository.b30()
            val r10 = potentialRepository.r10()
            val ptt = potentialRepository.potential()
            _uiState.value = UiState(isLoading = false, b30 = b30, r10 = r10, potential = ptt)
        }
    }
}
