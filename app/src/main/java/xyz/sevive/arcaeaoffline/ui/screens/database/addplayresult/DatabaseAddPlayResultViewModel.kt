package xyz.sevive.arcaeaoffline.ui.screens.database.addplayresult

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultClearType
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultModifier
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaScoringMode
import xyz.sevive.arcaeaoffline.core.database.entities.ChartInfo
import xyz.sevive.arcaeaoffline.core.database.entities.DifficultyWithSong
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.core.database.r30.ChartKey
import xyz.sevive.arcaeaoffline.core.database.repositories.ChartInfoRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.DifficultyWithSongRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.PlayResultRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.PropertyRepository
import xyz.sevive.arcaeaoffline.helpers.ArcaeaPlayResultValidator
import xyz.sevive.arcaeaoffline.helpers.ArcaeaPlayResultValidatorWarning
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class DatabaseAddPlayResultViewModel(
    private val difficultyWithSongRepo: DifficultyWithSongRepository,
    private val chartInfoRepo: ChartInfoRepository,
    private val playResultRepo: PlayResultRepository,
    propertyRepo: PropertyRepository,
) : ViewModel() {
    data class UiState(
        val chart: DifficultyWithSong? = null,
        val chartInfo: ChartInfo? = null,
        val playResult: PlayResult? = null,
        val scoringMode: ArcaeaScoringMode = ArcaeaScoringMode.B50,
        val warnings: List<ArcaeaPlayResultValidatorWarning> = emptyList(),
        val keepChartAfterSave: Boolean = false,
    )

    private val selectedChart = MutableStateFlow<ChartKey?>(null)
    private val editingPlayResult = MutableStateFlow<PlayResult?>(null)
    private val keepChartAfterSave = MutableStateFlow(false)

    private val chart: Flow<DifficultyWithSong?> =
        selectedChart.flatMapLatest { key ->
            key?.let { difficultyWithSongRepo.find(it.songId, it.ratingClass) } ?: flowOf(null)
        }

    private val chartInfo: Flow<ChartInfo?> =
        selectedChart.flatMapLatest { key ->
            key?.let { chartInfoRepo.find(it.songId, it.ratingClass) } ?: flowOf(null)
        }

    val uiState: StateFlow<UiState> =
        combine(
            chart,
            chartInfo,
            editingPlayResult,
            propertyRepo.scoringMode(),
            keepChartAfterSave,
        ) { chart, chartInfo, playResult, scoringMode, keepChartAfterSave ->
            UiState(
                chart = chart,
                chartInfo = chartInfo,
                playResult = playResult,
                scoringMode = scoringMode,
                warnings = playResult?.let { ArcaeaPlayResultValidator.validate(it, chartInfo, scoringMode) } ?: emptyList(),
                keepChartAfterSave = keepChartAfterSave,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5.seconds.inWholeMilliseconds),
            UiState(),
        )

    private fun freshPlayResult(key: ChartKey) =
        PlayResult(
            songId = key.songId,
            ratingClass = key.ratingClass,
            score = 0,
            pure = 0,
            far = 0,
            lost = 0,
            date = Clock.System.now(),
            maxRecall = 0,
            modifier = ArcaeaPlayResultModifier.NORMAL,
            clearType = ArcaeaPlayResultClearType.NORMAL_CLEAR,
            comment = null,
        )

    fun selectChart(
        songId: String,
        ratingClass: ArcaeaRatingClass,
    ) {
        val key = ChartKey(songId, ratingClass)
        if (selectedChart.value == key) return

        selectedChart.value = key
        editingPlayResult.value =
            editingPlayResult.value?.copy(songId = key.songId, ratingClass = key.ratingClass)
                ?: freshPlayResult(key)
    }

    fun setPlayResult(playResult: PlayResult) {
        editingPlayResult.value = playResult
    }

    fun setKeepChartAfterSave(keep: Boolean) {
        keepChartAfterSave.value = keep
    }

    fun reset() {
        selectedChart.value = null
        editingPlayResult.value = null
    }

    private var saveJob: Job? = null

    fun savePlayResult() {
        val playResult = editingPlayResult.value ?: return

        saveJob?.cancel()
        saveJob =
            viewModelScope.launch(Dispatchers.IO) {
                playResultRepo.upsert(playResult)

                if (keepChartAfterSave.value) {
                    editingPlayResult.value = selectedChart.value?.let(::freshPlayResult)
                } else {
                    reset()
                }
            }
    }
}
