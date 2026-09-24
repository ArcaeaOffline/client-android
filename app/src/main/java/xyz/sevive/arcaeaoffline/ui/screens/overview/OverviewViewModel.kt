package xyz.sevive.arcaeaoffline.ui.screens.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaScoringMode
import xyz.sevive.arcaeaoffline.core.database.repositories.PotentialRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.PropertyRepository
import kotlin.time.Duration.Companion.seconds

class OverviewViewModel(
    potentialRepository: PotentialRepository,
    propertyRepository: PropertyRepository,
) : ViewModel() {
    data class UiState(
        val isLoading: Boolean = true,
        val scoringMode: ArcaeaScoringMode = ArcaeaScoringMode.B50,
        val b30: Double? = null,
        val r10: Double? = null,
        val b50: Double? = null,
        val b10: Double? = null,
        val potential: Double? = null,
    )

    private data class SubPotentials(
        val b30: Double?,
        val r10: Double?,
        val b50: Double?,
        val b10: Double?,
    )

    val uiState =
        combine(
            propertyRepository.scoringMode(),
            potentialRepository.potential(),
            combine(
                potentialRepository.b30(),
                potentialRepository.r10(),
                potentialRepository.b50(),
                potentialRepository.b10(),
            ) { b30, r10, b50, b10 ->
                SubPotentials(b30, r10, b50, b10)
            },
        ) { scoringMode, potential, subPotentials ->
            UiState(
                isLoading = false,
                scoringMode = scoringMode,
                b30 = subPotentials.b30,
                r10 = subPotentials.r10,
                b50 = subPotentials.b50,
                b10 = subPotentials.b10,
                potential = potential,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5.seconds.inWholeMilliseconds),
            UiState(),
        )
}
