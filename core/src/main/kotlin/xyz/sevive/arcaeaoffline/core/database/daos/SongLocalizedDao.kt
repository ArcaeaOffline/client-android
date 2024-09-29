package xyz.sevive.arcaeaoffline.core.database.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import xyz.sevive.arcaeaoffline.core.database.entities.SongLocalized

@Dao
interface SongLocalizedDao {
    @Query("SELECT * FROM songs_localized WHERE id = :id")
    fun find(id: String): Flow<SongLocalized>

    @Query("SELECT * FROM songs_localized")
    fun findAll(): Flow<List<SongLocalized>>

    @Query("SELECT COUNT(*) FROM songs_localized")
    fun count(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: SongLocalized): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(vararg items: SongLocalized): List<Long>

    @Delete
    suspend fun delete(item: SongLocalized): Int

    @Delete
    suspend fun deleteBatch(vararg items: SongLocalized): Int
}
