package xyz.sevive.arcaeaoffline.core.database.daos

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow
import xyz.sevive.arcaeaoffline.core.database.entities.PackLocalized

@Dao
interface PackLocalizedDao {
    @Query("SELECT * FROM packs_localized WHERE id = :id")
    fun find(id: String): Flow<PackLocalized?>

    @Query("SELECT * FROM packs_localized")
    fun findAll(): Flow<List<PackLocalized>>

    @Query("SELECT COUNT(*) FROM packs_localized")
    fun count(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: PackLocalized): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(vararg items: PackLocalized): List<Long>

    @Delete
    suspend fun delete(item: PackLocalized): Int

    @Delete
    suspend fun deleteBatch(vararg items: PackLocalized): Int
}
