package xyz.sevive.arcaeaoffline.ui.screens.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import xyz.sevive.arcaeaoffline.core.database.repositories.PotentialRepository
import kotlin.time.Duration.Companion.seconds

class OverviewViewModel(potentialRepository: PotentialRepository) : ViewModel() {
    data class UiState(
        val isLoading: Boolean = true,
        val b30: Double? = null,
        val r10: Double? = null,
        val potential: Double? = null,
    )

    private fun calculatePotential(b30: Double, r10: Double) = b30 * 0.75 + r10 * 0.25

    val uiState = combine(
        potentialRepository.b30(),
        potentialRepository.r10(),
    ) { b30, r10 ->
        UiState(
            isLoading = false,
            b30 = b30,
            r10 = r10,
            potential = calculatePotential(b30 = b30, r10 = r10),
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5.seconds.inWholeMilliseconds),
        UiState(),
    )
}
