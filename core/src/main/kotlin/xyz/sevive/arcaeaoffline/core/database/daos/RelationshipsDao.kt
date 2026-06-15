package xyz.sevive.arcaeaoffline.core.database.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import kotlinx.coroutines.flow.Flow
import xyz.sevive.arcaeaoffline.core.database.entities.Chart
import xyz.sevive.arcaeaoffline.core.database.entities.MinimumPlayResultPotentialFields
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult

@Dao
interface RelationshipsDao {
    @RewriteQueriesToDropUnusedColumns
    @Query(
        """SELECT
    *
FROM
    play_results
    LEFT JOIN charts ON play_results.song_id = charts.song_id
    AND charts.rating_class = play_results.rating_class""",
    )
    fun playResultsWithCharts(): Flow<Map<PlayResult, Chart>>

    @Query(
        """SELECT
    pr.uuid,
    pr.song_id,
    pr.rating_class,
    pr.score,
    ci.constant
FROM
    play_results AS pr
    LEFT JOIN charts_info AS ci ON pr.song_id = ci.song_id
    AND pr.rating_class = ci.rating_class""",
    )
    fun minimumPlayResultPotentialFields(): Flow<List<MinimumPlayResultPotentialFields>>
}
