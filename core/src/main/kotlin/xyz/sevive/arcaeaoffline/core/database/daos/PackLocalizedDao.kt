package xyz.sevive.arcaeaoffline.core.database.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import xyz.sevive.arcaeaoffline.core.database.entities.PackLocalized

@Dao
interface PackLocalizedDao {
    @Query("SELECT * FROM packs_localized WHERE id = :id")
    fun find(id: String): Flow<PackLocalized>

    @Query("SELECT * FROM packs_localized")
    fun findAll(): Flow<List<PackLocalized>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: PackLocalized): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(vararg items: PackLocalized): List<Long>

    @Delete
    suspend fun delete(item: PackLocalized): Int

    @Delete
    suspend fun deleteBatch(vararg items: PackLocalized): Int
}
