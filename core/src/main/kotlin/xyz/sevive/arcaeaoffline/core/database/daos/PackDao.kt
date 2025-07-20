package xyz.sevive.arcaeaoffline.core.database.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import xyz.sevive.arcaeaoffline.core.database.entities.Pack


@Dao
interface PackDao {
    @Query("SELECT * FROM packs WHERE id = :id")
    fun find(id: String): Flow<Pack?>

    @Query("SELECT * FROM packs")
    fun findAll(): Flow<List<Pack>>

    @Query("SELECT COUNT(*) FROM packs")
    fun count(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: Pack): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBatch(vararg items: Pack): List<Long>

    @Delete
    suspend fun delete(item: Pack): Int

    @Delete
    suspend fun deleteBatch(vararg items: Pack): Int
}
