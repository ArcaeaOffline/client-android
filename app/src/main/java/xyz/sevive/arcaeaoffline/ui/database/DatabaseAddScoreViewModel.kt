package xyz.sevive.arcaeaoffline.ui.database

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import xyz.sevive.arcaeaoffline.constants.arcaea.score.ArcaeaScoreRatingClass
import xyz.sevive.arcaeaoffline.core.database.entities.Chart
import xyz.sevive.arcaeaoffline.core.database.entities.Score
import xyz.sevive.arcaeaoffline.core.database.helpers.ChartFactory
import xyz.sevive.arcaeaoffline.ui.containers.ArcaeaOfflineDatabaseRepositoryContainer

class DatabaseAddScoreViewModel(
    private val repositoryContainer: ArcaeaOfflineDatabaseRepositoryContainer
) : ViewModel() {
    private val _enabledRatingClasses = MutableStateFlow(listOf<ArcaeaScoreRatingClass>())
    val enabledRatingClasses = _enabledRatingClasses.asStateFlow()

    suspend fun updateEnabledRatingClasses(songId: String) {
        val difficulties =
            repositoryContainer.difficultyRepository.findAllBySongId(songId).firstOrNull()

        if (difficulties != null) {
            val ratingClassesInt = difficulties.map { it.ratingClass }
            _enabledRatingClasses.value =
                ratingClassesInt.map { ArcaeaScoreRatingClass.fromInt(it) }
        } else {
            _enabledRatingClasses.value = listOf()
        }
    }

    private val _chart = MutableStateFlow<Chart?>(null)
    val chart = _chart.asStateFlow()

    suspend fun setChart(songId: String, ratingClass: Int) {
        val chart = repositoryContainer.chartRepository.find(songId, ratingClass).firstOrNull()

        if (chart != null) {
            _chart.value = chart
        } else {
            val song = repositoryContainer.songRepository.find(songId).firstOrNull()
            val difficulty =
                repositoryContainer.difficultyRepository.find(songId, ratingClass).firstOrNull()

            if (song != null && difficulty != null) {
                _chart.value = ChartFactory.getChart(song, difficulty)
            } else {
                _chart.value = null
            }
        }
    }

    suspend fun saveScore(score: Score) {
        repositoryContainer.scoreRepository.upsert(score)
    }
}
