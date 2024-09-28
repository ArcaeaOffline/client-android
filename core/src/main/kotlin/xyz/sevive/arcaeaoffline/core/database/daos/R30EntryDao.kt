package xyz.sevive.arcaeaoffline.core.database.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import xyz.sevive.arcaeaoffline.core.database.entities.ChartInfo
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.core.database.entities.R30Entry

@Dao
interface R30EntryDao {
    @Query("SELECT * FROM r30_entries")
    fun findAll(): Flow<List<R30Entry>>

    @Query("SELECT * FROM r30_entries re JOIN play_results pr ON re.uuid = pr.uuid")
    fun findAllWithPlayResult(): Flow<Map<R30Entry, PlayResult>>

    @Query(
        "SELECT * FROM r30_entries re" +
            " JOIN play_results pr ON re.uuid = pr.uuid" +
            " JOIN charts_info ci ON ci.song_id = pr.song_id AND ci.rating_class = pr.rating_class"
    )
    fun findAllWithChartInfo(): Flow<Map<R30Entry, ChartInfo>>

    @Insert
    fun insert(item: R30Entry)

    @Insert
    fun insertBatch(vararg items: R30Entry)

    @Delete
    fun delete(item: R30Entry)

    @Delete
    fun deleteBatch(vararg items: R30Entry)

    /**
     * ```sql
     * DELETE FROM r30_entries
     * ```
     * CAUTION! ONLY CALL THIS WHEN YOU KNOW WHAT YOU ARE DOING!
     */
    @Query("DELETE FROM r30_entries")
    fun deleteAll()
}
