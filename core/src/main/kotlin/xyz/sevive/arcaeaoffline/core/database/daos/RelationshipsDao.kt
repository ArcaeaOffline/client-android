package xyz.sevive.arcaeaoffline.core.database.daos

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import xyz.sevive.arcaeaoffline.core.database.entities.Chart
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResultBest

@Dao
interface RelationshipsDao {
    @Query(
        "SELECT * FROM play_results LEFT JOIN charts" +
            " ON play_results.song_id = charts.song_id" +
            " AND charts.rating_class = play_results.rating_class"
    )
    fun playResultsWithCharts(): Flow<Map<PlayResult, Chart?>>

    @Query(
        "SELECT * FROM play_results_best LEFT JOIN charts" +
            " ON play_results_best.song_id = charts.song_id" +
            " AND charts.rating_class = play_results_best.rating_class" +
            " ORDER BY play_results_best.potential DESC LIMIT :limit"
    )
    fun playResultsBestWithCharts(limit: Int): Flow<Map<PlayResultBest, Chart?>>
}
