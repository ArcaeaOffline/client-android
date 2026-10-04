package xyz.sevive.arcaeaoffline.ui.screens.utilities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import xyz.sevive.arcaeaoffline.core.calculators.calculateClearBonus
import xyz.sevive.arcaeaoffline.core.calculators.calculatePlayRating
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultClearType
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaScoringMode
import xyz.sevive.arcaeaoffline.core.database.entities.DifficultyWithSongAndInfo
import xyz.sevive.arcaeaoffline.core.database.repositories.DifficultyWithSongRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.PotentialRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.PropertyRepository
import xyz.sevive.arcaeaoffline.helpers.calculatePotential
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.seconds

data class UtilitiesChartRecommendScreenUiState(
    val isLoading: Boolean = true,
    val scoreRange: IntRange = 9_800_000..9_899_999,
    val targetPlayRating: Double = 0.0,
    val scoringMode: ArcaeaScoringMode = PropertyRepository.DEFAULT_SCORING_MODE,
    val clearType: ArcaeaPlayResultClearType? = null,
    val charts: List<DifficultyWithSongAndInfo> = emptyList(),
)

/**
 * Charts whose single-play rating within [scoreRange], with the clear bonus of
 * [clearType], can reach [targetPlayRating].
 */
internal fun filterChartsByTarget(
    rows: List<DifficultyWithSongAndInfo>,
    scoreRange: IntRange,
    targetPlayRating: Double,
    clearType: ArcaeaPlayResultClearType?,
): List<DifficultyWithSongAndInfo> {
    val clearBonus = calculateClearBonus(clearType)

    return rows.filter { row ->
        val min = calculatePlayRating(score = scoreRange.first, constant = row.constant, clearBonus = clearBonus)
        val max = calculatePlayRating(score = scoreRange.last, constant = row.constant, clearBonus = clearBonus)
        targetPlayRating in min..max
    }
}

class UtilitiesChartRecommendScreenViewModel(
    private val difficultyWithSongRepo: DifficultyWithSongRepository,
    private val potentialRepo: PotentialRepository,
    private val propertyRepo: PropertyRepository,
) : ViewModel() {
    private val logger = Logger.withTag("UtilitiesChartRecommendScreenVM")

    private data class FilterParams(
        val scoreRange: IntRange,
        val targetPlayRating: Double,
        val clearType: ArcaeaPlayResultClearType?,
        val scoringMode: ArcaeaScoringMode,
    )

    private val scoreRange = MutableStateFlow(9_800_000..9_899_999)
    private val targetPlayRating = MutableStateFlow(0.0)
    private val clearType = MutableStateFlow<ArcaeaPlayResultClearType?>(null)

    init {
        viewModelScope.launch {
            // Seed the target with the current level of the active scoring rules
            val mode = propertyRepo.scoringMode().firstOrNull() ?: ArcaeaScoringMode.B50
            potentialRepo.groups().firstOrNull()?.let {
                val potential = mode.calculatePotential(it)
                targetPlayRating.value = ((potential + 0.05) * 100).roundToInt() / 100.0
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<UtilitiesChartRecommendScreenUiState> =
        combine(scoreRange, targetPlayRating, clearType, propertyRepo.scoringMode()) { sr, tpr, ct, mode ->
            FilterParams(sr, tpr, ct, mode)
        }.flatMapLatest { params ->
            // One joined query (ordered by constant) instead of fetching the
            // chart of each chart info row separately.
            difficultyWithSongRepo
                .findAllWithInfo()
                .map { rows ->
                    val effectiveClearType = if (params.scoringMode == ArcaeaScoringMode.B50) params.clearType else null

                    UtilitiesChartRecommendScreenUiState(
                        isLoading = false,
                        scoreRange = params.scoreRange,
                        targetPlayRating = params.targetPlayRating,
                        scoringMode = params.scoringMode,
                        clearType = effectiveClearType,
                        charts = filterChartsByTarget(rows, params.scoreRange, params.targetPlayRating, effectiveClearType),
                    )
                }.onStart {
                    emit(
                        UtilitiesChartRecommendScreenUiState(
                            isLoading = true,
                            scoreRange = params.scoreRange,
                            targetPlayRating = params.targetPlayRating,
                            scoringMode = params.scoringMode,
                            clearType = params.clearType,
                        ),
                    )
                }.catch { e ->
                    logger.e(e) { "Error fetching recommended charts" }
                    emit(
                        UtilitiesChartRecommendScreenUiState(
                            isLoading = false,
                            scoreRange = params.scoreRange,
                            targetPlayRating = params.targetPlayRating,
                            scoringMode = params.scoringMode,
                            clearType = params.clearType,
                            charts = emptyList(),
                        ),
                    )
                }
        }.scan(UtilitiesChartRecommendScreenUiState()) { oldState, newState ->
            // Keep old results while loading
            if (newState.isLoading) newState.copy(charts = oldState.charts) else newState
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5.seconds.inWholeMilliseconds),
            initialValue = UtilitiesChartRecommendScreenUiState(isLoading = true),
        )

    fun setScoreRange(newValue: IntRange) {
        scoreRange.value = newValue
    }

    fun setTargetPlayRating(newValue: Double) {
        targetPlayRating.value = newValue
    }

    fun setClearType(newValue: ArcaeaPlayResultClearType?) {
        clearType.value = newValue
    }
}
