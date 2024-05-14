package xyz.sevive.arcaeaoffline.ui.models

import androidx.lifecycle.ViewModel
import xyz.sevive.arcaeaoffline.core.database.entities.Chart
import xyz.sevive.arcaeaoffline.core.database.entities.Score
import xyz.sevive.arcaeaoffline.core.helpers.ChartFactory
import xyz.sevive.arcaeaoffline.ui.containers.ArcaeaOfflineDatabaseRepositoryContainer

class DatabaseCommonFunctionsViewModel(
    private val arcaeaOfflineDatabaseRepositoryContainer: ArcaeaOfflineDatabaseRepositoryContainer
) : ViewModel() {
//    val chartList = arcaeaOfflineDatabaseRepositoryContainer.chartRepository.findAll().stateIn(
//        scope = viewModelScope,
//        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
//        initialValue = listOf(),
//    )

    suspend fun findChart(songId: String, ratingClass: Int): Chart? {
        return ChartFactory.getChart(arcaeaOfflineDatabaseRepositoryContainer, songId, ratingClass)
    }

//    suspend fun findChart(songId: String, ratingClass: ArcaeaScoreRatingClass): Chart? {
//        return ChartFactory.getChart(arcaeaOfflineDatabaseRepositoryContainer, songId, ratingClass)
//    }

    suspend fun upsertScore(score: Score) {
        arcaeaOfflineDatabaseRepositoryContainer.scoreRepository.upsert(score)
    }

//    companion object {
//        const val STOP_TIMEOUT_MILLIS = 10000L
//    }
}
