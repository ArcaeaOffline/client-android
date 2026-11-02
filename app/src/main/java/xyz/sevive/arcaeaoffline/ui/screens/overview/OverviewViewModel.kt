package xyz.sevive.arcaeaoffline.ui.screens.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaScoringMode
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.core.database.repositories.DatabaseReadiness
import xyz.sevive.arcaeaoffline.core.database.repositories.DatabaseReadinessRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.DatabaseReadinessSummary
import xyz.sevive.arcaeaoffline.core.database.repositories.PlayResultRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.PotentialGroups
import xyz.sevive.arcaeaoffline.core.database.repositories.PotentialRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.PropertyRepository
import xyz.sevive.arcaeaoffline.helpers.calculatePotential
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class OverviewViewModel(
    potentialRepository: PotentialRepository,
    propertyRepository: PropertyRepository,
    readinessRepository: DatabaseReadinessRepository,
    playResultRepository: PlayResultRepository,
) : ViewModel() {
    companion object {
        private const val LATEST_PLAYS_LIMIT = 5
    }

    data class UiState(
        val isLoading: Boolean = true,
        val scoringMode: ArcaeaScoringMode = ArcaeaScoringMode.B50,
        val groups: PotentialGroups? = null,
        val potential: Double? = null,
    )

    data class ReadinessUiState(
        val summary: DatabaseReadinessSummary =
            DatabaseReadinessSummary(
                readiness = DatabaseReadiness.EMPTY,
                songCount = 0,
                chartInfoCount = 0,
                missingChartInfoCount = 0,
            ),
        /** The later of the songs and chart info import timestamps. */
        val lastImportedAt: Instant? = null,
    )

    val uiState =
        combine(
            propertyRepository.scoringMode(),
            potentialRepository.groups(),
        ) { scoringMode, groups ->
            UiState(
                isLoading = false,
                scoringMode = scoringMode,
                groups = groups,
                potential = scoringMode.calculatePotential(groups),
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5.seconds.inWholeMilliseconds),
            UiState(),
        )

    val readinessUiState: StateFlow<ReadinessUiState> =
        combine(
            readinessRepository.summary(),
            propertyRepository.songsLastImportedAt(),
            propertyRepository.chartInfoLastImportedAt(),
        ) { summary, songsLastImportedAt, chartInfoLastImportedAt ->
            ReadinessUiState(
                summary = summary,
                lastImportedAt = listOfNotNull(songsLastImportedAt, chartInfoLastImportedAt).maxOrNull(),
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5.seconds.inWholeMilliseconds),
            ReadinessUiState(),
        )

    val latestPlays: StateFlow<List<PlayResult>> =
        playResultRepository
            .findLatestPlays(LATEST_PLAYS_LIMIT)
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5.seconds.inWholeMilliseconds),
                emptyList(),
            )
}
