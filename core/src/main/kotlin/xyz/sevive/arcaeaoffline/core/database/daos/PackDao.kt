package xyz.sevive.arcaeaoffline.core.database.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import xyz.sevive.arcaeaoffline.core.database.entities.Pack


@Dao
interface PackDao {
    @Query("SELECT * FROM packs WHERE id = :id")
    fun find(id: String): Flow<Pack>

    @Query("SELECT * FROM packs")
    fun findAll(): Flow<List<Pack>>

    @Upsert
    suspend fun upsert(item: Pack): Long

    @Upsert
    suspend fun upsertAll(vararg items: Pack): List<Long>

    @Delete
    suspend fun delete(item: Pack): Int

    @Delete
    suspend fun deleteAll(vararg items: Pack): Int
}



