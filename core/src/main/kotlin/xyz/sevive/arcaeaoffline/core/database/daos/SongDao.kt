package xyz.sevive.arcaeaoffline.core.database.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
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

    @Upsert
    suspend fun upsert(item: Song)

    @Upsert
    suspend fun upsertAll(vararg items: Song): LongArray

    @Delete
    suspend fun delete(item: Song)

    @Delete
    suspend fun deleteAll(vararg items: Song)
}


