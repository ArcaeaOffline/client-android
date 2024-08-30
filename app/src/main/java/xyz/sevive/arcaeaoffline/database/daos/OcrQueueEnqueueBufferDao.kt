package xyz.sevive.arcaeaoffline.database.daos

import android.net.Uri
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import xyz.sevive.arcaeaoffline.database.entities.OcrQueueEnqueueBuffer


@Dao
interface OcrQueueEnqueueBufferDao {
    @Query("SELECT * FROM ocr_queue_enqueue_buffer WHERE checked = 0")
    fun findUnchecked(): Flow<List<OcrQueueEnqueueBuffer>>

    @Query("SELECT * FROM ocr_queue_enqueue_buffer WHERE checked = 1")
    fun findChecked(): Flow<List<OcrQueueEnqueueBuffer>>

    @Query("SELECT uri FROM ocr_queue_enqueue_buffer WHERE checked = 1 AND should_insert = 1")
    fun findShouldInsertUris(): Flow<List<Uri>>

    @Query("SELECT COUNT(*) FROM ocr_queue_enqueue_buffer")
    fun count(): Flow<Int>

    @Query("SELECT COUNT(*) FROM ocr_queue_enqueue_buffer WHERE checked = 1")
    fun countChecked(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(ocrQueueEnqueueBuffer: OcrQueueEnqueueBuffer): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBatch(ocrQueueEnqueueBuffers: List<OcrQueueEnqueueBuffer>): List<Long>

    @Update
    suspend fun update(ocrQueueEnqueueBuffer: OcrQueueEnqueueBuffer)

    @Query("DELETE FROM ocr_queue_enqueue_buffer WHERE checked = 1")
    suspend fun deleteChecked(): Int

    @Query("DELETE FROM ocr_queue_enqueue_buffer")
    suspend fun deleteAll(): Int
}
