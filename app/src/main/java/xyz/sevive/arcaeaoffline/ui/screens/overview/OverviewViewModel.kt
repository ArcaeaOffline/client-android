package xyz.sevive.arcaeaoffline.ui.screens.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaScoringMode
import xyz.sevive.arcaeaoffline.core.database.repositories.PotentialGroups
import xyz.sevive.arcaeaoffline.core.database.repositories.PotentialRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.PropertyRepository
import xyz.sevive.arcaeaoffline.helpers.calculatePotential
import kotlin.time.Duration.Companion.seconds

class OverviewViewModel(
    potentialRepository: PotentialRepository,
    propertyRepository: PropertyRepository,
) : ViewModel() {
    data class UiState(
        val isLoading: Boolean = true,
        val scoringMode: ArcaeaScoringMode = ArcaeaScoringMode.B50,
        val entries: PotentialGroups? = null,
        val potential: Double? = null,
    )

    val uiState =
        combine(
            propertyRepository.scoringMode(),
            potentialRepository.groups(),
        ) { scoringMode, entries ->
            UiState(
                isLoading = false,
                scoringMode = scoringMode,
                entries = entries,
                potential = scoringMode.calculatePotential(entries),
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5.seconds.inWholeMilliseconds),
            UiState(),
        )
}
