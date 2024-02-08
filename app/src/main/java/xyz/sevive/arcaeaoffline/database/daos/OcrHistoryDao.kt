package xyz.sevive.arcaeaoffline.database.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import xyz.sevive.arcaeaoffline.database.entities.OcrHistory


@Dao
interface OcrHistoryDao {
    @Query("SELECT * FROM ocr_history")
    fun findAll(): Flow<List<OcrHistory>>

    @Query("SELECT * FROM ocr_history WHERE id = :id")
    fun findById(id: Int): Flow<OcrHistory>

    @Insert
    suspend fun insert(item: OcrHistory): Long

    @Insert
    suspend fun insertAll(vararg items: OcrHistory)

    @Delete
    suspend fun delete(item: OcrHistory)
}
