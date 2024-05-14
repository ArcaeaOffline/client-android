package xyz.sevive.arcaeaoffline.ui.database

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import xyz.sevive.arcaeaoffline.core.database.entities.Chart
import xyz.sevive.arcaeaoffline.core.database.entities.Score
import xyz.sevive.arcaeaoffline.helpers.ArcaeaScoreValidator
import xyz.sevive.arcaeaoffline.helpers.ArcaeaScoreValidatorWarning
import xyz.sevive.arcaeaoffline.ui.containers.ArcaeaOfflineDatabaseRepositoryContainer

class DatabaseAddScoreViewModel(
    private val repositoryContainer: ArcaeaOfflineDatabaseRepositoryContainer
) : ViewModel() {
    private val _chart = MutableStateFlow<Chart?>(null)
    val chart = _chart.asStateFlow()

    fun setChart(chart: Chart?) {
        _chart.value = chart
        initScore()
    }

    private val _score = MutableStateFlow<Score?>(null)
    val score = _score.asStateFlow()

    private val _scoreWarnings = MutableStateFlow<List<ArcaeaScoreValidatorWarning>>(listOf())
    val scoreWarnings = _scoreWarnings.asStateFlow()

    private fun validateScore() {
        val score = score.value ?: return
        val chart = chart.value

        _scoreWarnings.value = ArcaeaScoreValidator.validateScore(score = score, chart = chart)
    }

    private fun initScore() {
        val songId = chart.value?.songId
        val ratingClass = chart.value?.ratingClass

        if (songId == null || ratingClass == null) {
            setScore(null)
            return
        }

        setScore(
            score = if (_score.value != null) {
                _score.value!!.copy(songId = songId, ratingClass = ratingClass)
            } else {
                Score(songId = songId, ratingClass = ratingClass, score = 0)
            }
        )
    }

    fun setScore(score: Score?) {
        _score.value = score
        validateScore()
    }

    fun reset() {
        _chart.value = null
        _score.value = null
    }

    suspend fun saveScore() {
        if (score.value != null) {
            repositoryContainer.scoreRepository.upsert(score.value!!)
            reset()
        }
    }
}
