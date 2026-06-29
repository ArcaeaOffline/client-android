package xyz.sevive.arcaeaoffline.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import xyz.sevive.arcaeaoffline.database.entities.OcrQueueStagingItem
import xyz.sevive.arcaeaoffline.database.entities.OcrQueueStagingUriType

@Dao
interface OcrQueueStagingItemDao {
    @Query("SELECT * FROM ocr_queue_staging_item WHERE uri_type = :type")
    suspend fun findByUriType(type: OcrQueueStagingUriType): List<OcrQueueStagingItem>

    @Query("SELECT * FROM ocr_queue_staging_item WHERE checked = 1 AND should_insert = 1")
    suspend fun findShouldInsertItems(): List<OcrQueueStagingItem>

    @Query("SELECT COUNT(*) FROM ocr_queue_staging_item")
    fun count(): Flow<Int>

    @Query("SELECT COUNT(*) FROM ocr_queue_staging_item WHERE checked = 1")
    fun countChecked(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: OcrQueueStagingItem): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBatch(items: List<OcrQueueStagingItem>): List<Long>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(item: OcrQueueStagingItem)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateBatch(items: List<OcrQueueStagingItem>)

    @Query("DELETE FROM ocr_queue_staging_item WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM ocr_queue_staging_item WHERE checked = 1")
    suspend fun deleteChecked(): Int

    @Query("DELETE FROM ocr_queue_staging_item")
    suspend fun deleteAll(): Int
}
