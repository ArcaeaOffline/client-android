package xyz.sevive.arcaeaoffline.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import xyz.sevive.arcaeaoffline.database.entities.OcrQueueStagingBatch
import xyz.sevive.arcaeaoffline.database.entities.OcrQueueStagingBatchWithItems

@Dao
interface OcrQueueStagingBatchDao {
    @Transaction
    @Query("SELECT * FROM ocr_queue_staging_batch")
    suspend fun getAllBatchWithItems(): List<OcrQueueStagingBatchWithItems>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: OcrQueueStagingBatch): Long

    @Query("DELETE FROM ocr_queue_staging_batch WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    @Query("SELECT * FROM ocr_queue_staging_batch WHERE id NOT IN (SELECT batch_id FROM ocr_queue_staging_item)")
    suspend fun getOrphans(): List<OcrQueueStagingBatch>
}
