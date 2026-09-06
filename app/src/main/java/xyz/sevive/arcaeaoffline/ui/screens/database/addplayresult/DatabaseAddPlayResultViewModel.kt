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
import xyz.sevive.arcaeaoffline.core.database.entities.Difficulty
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.core.database.repositories.ChartInfoRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.PlayResultRepository
import xyz.sevive.arcaeaoffline.helpers.ArcaeaPlayResultValidator
import xyz.sevive.arcaeaoffline.helpers.ArcaeaPlayResultValidatorWarning
import kotlin.time.Duration.Companion.seconds

class DatabaseAddPlayResultViewModel(
    private val chartInfoRepo: ChartInfoRepository,
    private val playResultRepo: PlayResultRepository,
) : ViewModel() {
    data class UiState(
        val difficulty: Difficulty? = null,
        val playResult: PlayResult? = null,
        val warnings: List<ArcaeaPlayResultValidatorWarning> = emptyList(),
    )

    private val _difficulty = MutableStateFlow<Difficulty?>(null)
    val difficulty = _difficulty.asStateFlow()

    private val _playResult = MutableStateFlow<PlayResult?>(null)
    val playResult = _playResult.asStateFlow()

    private suspend fun getPlayResultWarnings(playResult: PlayResult?): List<ArcaeaPlayResultValidatorWarning> {
        if (playResult == null) return emptyList()

        val chartInfo = chartInfoRepo.find(playResult).firstOrNull()
        return ArcaeaPlayResultValidator.validate(playResult, chartInfo)
    }

    val uiState =
        combine(difficulty, playResult) { difficulty, playResult ->
            UiState(
                difficulty = difficulty,
                playResult = playResult,
                warnings = getPlayResultWarnings(playResult),
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5.seconds.inWholeMilliseconds),
            UiState(),
        )

    fun setDifficulty(difficulty: Difficulty?) {
        _difficulty.value = difficulty
        initPlayResult()
    }

    private fun initPlayResult() {
        setPlayResult(
            _difficulty.value?.let {
                _playResult.value?.copy(songId = it.songId, ratingClass = it.ratingClass)
                    ?: PlayResult(songId = it.songId, ratingClass = it.ratingClass, score = 0)
            },
        )
    }

    fun setPlayResult(playResult: PlayResult?) {
        _playResult.value = playResult
    }

    fun reset() {
        _difficulty.value = null
        _playResult.value = null
    }

    private var savePlayResultJob: Job? = null

    fun savePlayResult() {
        savePlayResultJob?.cancel()
        if (playResult.value == null) return

        savePlayResultJob =
            viewModelScope.launch(Dispatchers.IO) {
                playResult.value?.let {
                    playResultRepo.upsert(it)
                    reset()
                }
            }
    }
}
