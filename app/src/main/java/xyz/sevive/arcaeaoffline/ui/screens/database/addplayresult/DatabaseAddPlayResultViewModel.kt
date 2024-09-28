package xyz.sevive.arcaeaoffline.ui.screens.database.addplayresult

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import xyz.sevive.arcaeaoffline.core.database.entities.Chart
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.helpers.ArcaeaPlayResultValidator
import xyz.sevive.arcaeaoffline.helpers.ArcaeaPlayResultValidatorWarning
import xyz.sevive.arcaeaoffline.ui.containers.ArcaeaOfflineDatabaseRepositoryContainer
import kotlin.time.Duration.Companion.seconds

class DatabaseAddPlayResultViewModel(
    private val repositoryContainer: ArcaeaOfflineDatabaseRepositoryContainer
) : ViewModel() {
    data class UiState(
        val chart: Chart? = null,
        val playResult: PlayResult? = null,
        val warnings: List<ArcaeaPlayResultValidatorWarning> = emptyList(),
    )

    private val _chart = MutableStateFlow<Chart?>(null)
    val chart = _chart.asStateFlow()

    private val _playResult = MutableStateFlow<PlayResult?>(null)
    val playResult = _playResult.asStateFlow()

    private suspend fun getPlayResultWarnings(playResult: PlayResult?): List<ArcaeaPlayResultValidatorWarning> {
        if (playResult == null) return emptyList()

        val chartInfo = repositoryContainer.chartInfoRepo.find(playResult).firstOrNull()
        return ArcaeaPlayResultValidator.validateScore(playResult, chartInfo)
    }

    val uiState = combine(chart, playResult) { chart, playResult ->
        UiState(
            chart = chart,
            playResult = playResult,
            warnings = getPlayResultWarnings(playResult),
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5.seconds.inWholeMilliseconds),
        UiState(),
    )

    fun setChart(chart: Chart?) {
        _chart.value = chart
        initPlayResult()
    }

    private fun initPlayResult() {
        setPlayResult(
            _chart.value?.let {
                _playResult.value?.copy(songId = it.songId, ratingClass = it.ratingClass)
                    ?: PlayResult(songId = it.songId, ratingClass = it.ratingClass, score = 0)
            }
        )
    }

    fun setPlayResult(playResult: PlayResult?) {
        _playResult.value = playResult
    }

    fun reset() {
        _chart.value = null
        _playResult.value = null
    }

    private var savePlayResultJob: Job? = null
    fun savePlayResult() {
        savePlayResultJob?.cancel()
        if (playResult.value == null) return

        savePlayResultJob = viewModelScope.launch(Dispatchers.IO) {
            playResult.value?.let {
                repositoryContainer.playResultRepo.upsert(it)
                reset()
            }
        }
    }
}
