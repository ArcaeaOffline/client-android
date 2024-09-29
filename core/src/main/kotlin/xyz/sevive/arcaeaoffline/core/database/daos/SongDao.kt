package xyz.sevive.arcaeaoffline.core.database.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import xyz.sevive.arcaeaoffline.core.database.entities.Song

@Dao
interface SongDao {
    @Query("SELECT * FROM songs WHERE id = :id")
    fun find(id: String): Flow<Song>

    @Query("SELECT * FROM songs WHERE `set` = :set")
    fun findBySet(set: String): Flow<List<Song>>

    @Query("SELECT * FROM songs")
    fun findAll(): Flow<List<Song>>

    @Query("SELECT COUNT(*) FROM songs")
    fun count(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: Song): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBatch(vararg items: Song): List<Long>

    @Delete
    suspend fun delete(item: Song): Int

    @Delete
    suspend fun deleteBatch(vararg items: Song): Int
}
