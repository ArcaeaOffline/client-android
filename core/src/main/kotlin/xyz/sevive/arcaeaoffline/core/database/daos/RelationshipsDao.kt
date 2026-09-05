package xyz.sevive.arcaeaoffline.core.database.daos

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import xyz.sevive.arcaeaoffline.core.database.entities.Chart
import xyz.sevive.arcaeaoffline.core.database.entities.MinimumPlayResultPotentialFields
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult

@Dao
interface RelationshipsDao {
    // pr.* shadows song_id/rating_class/date; chart.songId and chart.ratingClass
    // are correct by join equality, chart.date is not read by consumers of this
    // query.
    @Query(
        """SELECT
            pr.*,
            s.idx AS song_idx, d.rating, d.rating_plus, d.rating_class_alias,
            $CHART_DISPLAY_COLUMNS,
            ci.constant, ci.notes
        FROM play_results pr
        LEFT JOIN difficulties d ON pr.song_id = d.song_id AND pr.rating_class = d.rating_class
        LEFT JOIN songs s ON d.song_id = s.id
        LEFT JOIN charts_info ci ON d.song_id = ci.song_id AND d.rating_class = ci.rating_class""",
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
