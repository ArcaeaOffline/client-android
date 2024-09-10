package xyz.sevive.arcaeaoffline.database.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import xyz.sevive.arcaeaoffline.database.entities.OcrQueueTask
import xyz.sevive.arcaeaoffline.database.entities.OcrQueueTaskStatus


@Dao
interface OcrQueueTaskDao {
    @Query("SELECT * FROM ocr_queue_tasks")
    fun findAll(): Flow<List<OcrQueueTask>>

    @Query("SELECT * FROM ocr_queue_tasks WHERE id = :id")
    fun findById(id: Long): Flow<OcrQueueTask>

    @Query("SELECT * FROM ocr_queue_tasks WHERE status IN (:statuses)")
    fun findByStatus(statuses: List<OcrQueueTaskStatus>): Flow<List<OcrQueueTask>>

    @Query("SELECT * FROM ocr_queue_tasks WHERE status = 3 AND warnings IS NOT NULL")
    fun findDoneWithWarning(): Flow<List<OcrQueueTask>>

    @Query("SELECT COUNT(*) FROM ocr_queue_tasks")
    fun count(): Flow<Int>

    @Query("SELECT COUNT(*) FROM ocr_queue_tasks WHERE status IN (:statuses)")
    fun countByStatus(statuses: List<OcrQueueTaskStatus>): Flow<Int>

    @Query("SELECT COUNT(*) FROM ocr_queue_tasks WHERE status = 3 AND warnings IS NOT NULL")
    fun countDoneWithWarning(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: OcrQueueTask): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBatch(items: List<OcrQueueTask>): List<Long>

    @Update
    suspend fun update(item: OcrQueueTask): Int

    @Delete
    suspend fun delete(item: OcrQueueTask): Int

    @Query("DELETE FROM ocr_queue_tasks WHERE id = :id")
    suspend fun delete(id: Long): Int

    @Delete
    suspend fun deleteBatch(items: List<OcrQueueTask>): Int

    @Query("DELETE FROM ocr_queue_tasks")
    suspend fun deleteAll()
}
