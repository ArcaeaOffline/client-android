package xyz.sevive.arcaeaoffline.core.database.daos

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.database.entities.DifficultyLocalized

@Dao
interface DifficultyLocalizedDao {
    @Query("SELECT * FROM difficulties_localized WHERE song_id = :songId AND rating_class = :ratingClass")
    fun find(
        songId: String,
        ratingClass: ArcaeaRatingClass,
    ): Flow<DifficultyLocalized?>

    @Query("SELECT * FROM difficulties_localized")
    fun findAll(): Flow<List<DifficultyLocalized>>

    @Query("SELECT * FROM difficulties_localized WHERE song_id = :songId")
    fun findAllBySongId(songId: String): Flow<List<DifficultyLocalized>>

    @Query("SELECT COUNT(*) FROM difficulties_localized")
    fun count(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: DifficultyLocalized): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(vararg items: DifficultyLocalized): List<Long>

    @Delete
    suspend fun delete(item: DifficultyLocalized): Int

    @Delete
    suspend fun deleteBatch(vararg items: DifficultyLocalized): Int
}
