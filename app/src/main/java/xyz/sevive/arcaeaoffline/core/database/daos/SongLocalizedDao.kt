package xyz.sevive.arcaeaoffline.core.database.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import xyz.sevive.arcaeaoffline.core.database.entities.SongLocalized

@Dao
interface SongLocalizedDao {
    @Query("SELECT * FROM songs_localized WHERE id = :id")
    fun find(id: String): Flow<SongLocalized>

    @Query("SELECT * FROM songs_localized")
    fun findAll(): Flow<List<SongLocalized>>

    @Upsert
    suspend fun upsert(item: SongLocalized)

    @Upsert
    suspend fun upsertAll(vararg items: SongLocalized)

    @Delete
    suspend fun delete(item: SongLocalized)

    @Delete
    suspend fun deleteAll(vararg items: SongLocalized)
}


