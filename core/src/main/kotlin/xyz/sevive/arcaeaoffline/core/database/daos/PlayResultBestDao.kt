package xyz.sevive.arcaeaoffline.core.database.daos

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import xyz.sevive.arcaeaoffline.core.database.entities.MinimumPlayResultPotentialFields

@Dao
interface PlayResultBestDao {
    // Minimum fields to compute play rating per play result; full
    // PlayResultCalculated rows are fetched afterwards for the best
    // uuids only (see PlayResultBestRepository).
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
