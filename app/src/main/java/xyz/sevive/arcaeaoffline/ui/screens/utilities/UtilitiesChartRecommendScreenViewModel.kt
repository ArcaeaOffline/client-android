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
import xyz.sevive.arcaeaoffline.core.calculators.calculatePlayRating
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaScoringMode
import xyz.sevive.arcaeaoffline.core.database.entities.DifficultyWithSongAndInfo
import xyz.sevive.arcaeaoffline.core.database.repositories.DifficultyWithSongRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.PotentialRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.PropertyRepository
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.seconds

data class UtilitiesChartRecommendScreenUiState(
    val isLoading: Boolean = true,
    val scoreRange: IntRange = 9_800_000..9_899_999,
    val targetPlayRating: Double = 0.0,
    val charts: List<DifficultyWithSongAndInfo> = emptyList(),
)

class UtilitiesChartRecommendScreenViewModel(
    private val difficultyWithSongRepo: DifficultyWithSongRepository,
    private val potentialRepo: PotentialRepository,
    private val propertyRepo: PropertyRepository,
) : ViewModel() {
    private val logger = Logger.withTag("UtilitiesChartRecommendScreenVM")

    private data class FilterParams(
        val scoreRange: IntRange,
        val targetPlayRating: Double,
    )

    private val scoreRange = MutableStateFlow(9_800_000..9_899_999)
    private val targetPlayRating = MutableStateFlow(0.0)

    init {
        viewModelScope.launch {
            // Seed the target with the current level of the active scoring rules
            val mode = propertyRepo.scoringMode().firstOrNull() ?: ArcaeaScoringMode.B50
            val currentPotential =
                when (mode) {
                    ArcaeaScoringMode.B30_R10 -> potentialRepo.b30().firstOrNull()
                    ArcaeaScoringMode.B50 -> potentialRepo.b50().firstOrNull()
                }

            currentPotential?.let {
                targetPlayRating.value = ((it + 0.05) * 100).roundToInt() / 100.0
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<UtilitiesChartRecommendScreenUiState> =
        combine(scoreRange, targetPlayRating) { sr, tpr ->
            FilterParams(sr, tpr)
        }.flatMapLatest { params ->
            // One joined query (ordered by constant) instead of fetching the
            // chart of each chart info row separately.
            difficultyWithSongRepo
                .findAllWithInfo()
                .map { rows ->
                    val filtered =
                        rows
                            .filter { row ->
                                val min = calculatePlayRating(score = params.scoreRange.first, constant = row.constant)
                                val max = calculatePlayRating(score = params.scoreRange.last, constant = row.constant)
                                params.targetPlayRating in min..max
                            }

                    UtilitiesChartRecommendScreenUiState(
                        isLoading = false,
                        scoreRange = params.scoreRange,
                        targetPlayRating = params.targetPlayRating,
                        charts = filtered,
                    )
                }.onStart {
                    emit(
                        UtilitiesChartRecommendScreenUiState(
                            isLoading = true,
                            scoreRange = params.scoreRange,
                            targetPlayRating = params.targetPlayRating,
                        ),
                    )
                }.catch { e ->
                    logger.e(e) { "Error fetching recommended charts" }
                    emit(
                        UtilitiesChartRecommendScreenUiState(
                            isLoading = false,
                            scoreRange = params.scoreRange,
                            targetPlayRating = params.targetPlayRating,
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
}
