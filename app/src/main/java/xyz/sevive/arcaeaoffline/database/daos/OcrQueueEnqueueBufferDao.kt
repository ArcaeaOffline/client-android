package xyz.sevive.arcaeaoffline.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import xyz.sevive.arcaeaoffline.database.entities.OcrQueueEnqueueBuffer
import xyz.sevive.arcaeaoffline.database.entities.OcrQueueUriType

@Dao
interface OcrQueueEnqueueBufferDao {
    @Query("SELECT * FROM ocr_queue_enqueue_buffer WHERE uri_type = :type")
    suspend fun findByUriType(type: OcrQueueUriType): List<OcrQueueEnqueueBuffer>

    @Query("SELECT * FROM ocr_queue_enqueue_buffer WHERE checked = 1 AND should_insert = 1")
    suspend fun findShouldInsertBuffers(): List<OcrQueueEnqueueBuffer>

    @Query("SELECT COUNT(*) FROM ocr_queue_enqueue_buffer")
    fun count(): Flow<Int>

    @Query("SELECT COUNT(*) FROM ocr_queue_enqueue_buffer WHERE checked = 1")
    fun countChecked(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: OcrQueueEnqueueBuffer): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBatch(items: List<OcrQueueEnqueueBuffer>): List<Long>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(item: OcrQueueEnqueueBuffer)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateBatch(items: List<OcrQueueEnqueueBuffer>)

    @Query("DELETE FROM ocr_queue_enqueue_buffer WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM ocr_queue_enqueue_buffer WHERE checked = 1")
    suspend fun deleteChecked(): Int

    @Query("DELETE FROM ocr_queue_enqueue_buffer")
    suspend fun deleteAll(): Int
}
