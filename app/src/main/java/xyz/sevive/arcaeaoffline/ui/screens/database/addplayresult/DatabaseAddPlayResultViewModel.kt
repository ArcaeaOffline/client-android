package xyz.sevive.arcaeaoffline.ui.screens.database.addplayresult

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import xyz.sevive.arcaeaoffline.core.database.entities.Chart
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.helpers.ArcaeaPlayResultValidator
import xyz.sevive.arcaeaoffline.helpers.ArcaeaPlayResultValidatorWarning
import xyz.sevive.arcaeaoffline.ui.containers.ArcaeaOfflineDatabaseRepositoryContainer

class DatabaseAddPlayResultViewModel(
    private val repositoryContainer: ArcaeaOfflineDatabaseRepositoryContainer
) : ViewModel() {
    private val _chart = MutableStateFlow<Chart?>(null)
    val chart = _chart.asStateFlow()

    fun setChart(chart: Chart?) {
        _chart.value = chart
        initScore()
    }

    private val _playResult = MutableStateFlow<PlayResult?>(null)
    val playResult = _playResult.asStateFlow()

    private val _scoreWarnings = MutableStateFlow<List<ArcaeaPlayResultValidatorWarning>>(listOf())
    val warnings = _scoreWarnings.asStateFlow()

    private fun validateScore() {
        val score = playResult.value ?: return
        val chart = chart.value

        _scoreWarnings.value =
            ArcaeaPlayResultValidator.validateScore(playResult = score, chart = chart)
    }

    private fun initScore() {
        val songId = chart.value?.songId
        val ratingClass = chart.value?.ratingClass

        if (songId == null || ratingClass == null) {
            setScore(null)
            return
        }

        setScore(
            playResult = if (_playResult.value != null) {
                _playResult.value!!.copy(songId = songId, ratingClass = ratingClass)
            } else {
                PlayResult(songId = songId, ratingClass = ratingClass, score = 0)
            }
        )
    }

    fun setScore(playResult: PlayResult?) {
        _playResult.value = playResult
        validateScore()
    }

    fun reset() {
        _chart.value = null
        _playResult.value = null
    }

    suspend fun saveScore() {
        if (playResult.value != null) {
            repositoryContainer.playResultRepo.upsert(playResult.value!!)
            reset()
        }
    }
}
