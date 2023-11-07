package xyz.sevive.arcaeaoffline.core.database.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import xyz.sevive.arcaeaoffline.core.database.entities.PackLocalized

@Dao
interface PackLocalizedDao {
    @Query("SELECT * FROM packs_localized WHERE id = :id")
    fun find(id: String): Flow<PackLocalized>

    @Query("SELECT * FROM packs_localized")
    fun findAll(): Flow<List<PackLocalized>>

    @Upsert
    suspend fun upsert(item: PackLocalized)

    @Upsert
    suspend fun upsertAll(vararg items: PackLocalized)

    @Delete
    suspend fun delete(item: PackLocalized)

    @Delete
    suspend fun deleteAll(vararg items: PackLocalized)
}


