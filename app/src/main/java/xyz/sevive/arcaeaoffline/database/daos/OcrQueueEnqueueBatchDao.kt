package xyz.sevive.arcaeaoffline.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import xyz.sevive.arcaeaoffline.database.entities.OcrQueueBatchWithBuffers
import xyz.sevive.arcaeaoffline.database.entities.OcrQueueEnqueueBatch

@Dao
interface OcrQueueEnqueueBatchDao {
    @Transaction
    @Query("SELECT * FROM ocr_queue_enqueue_batches")
    suspend fun getAllBatchWithBuffers(): List<OcrQueueBatchWithBuffers>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: OcrQueueEnqueueBatch): Long

    @Query("DELETE FROM ocr_queue_enqueue_batches WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    @Query("SELECT * FROM ocr_queue_enqueue_batches WHERE id NOT IN (SELECT batch_id FROM ocr_queue_enqueue_buffer)")
    suspend fun getOrphans(): List<OcrQueueEnqueueBatch>
}
