package xyz.sevive.arcaeaoffline.core.database.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.database.entities.Difficulty

@Dao
interface DifficultyDao {
    @Query("SELECT * FROM difficulties WHERE song_id = :songId AND rating_class = :ratingClass")
    fun find(songId: String, ratingClass: ArcaeaRatingClass): Flow<Difficulty>

    @Query("SELECT * FROM difficulties")
    fun findAll(): Flow<List<Difficulty>>

    @Query("SELECT * FROM difficulties WHERE song_id = :songId")
    fun findAllBySongId(songId: String): Flow<List<Difficulty>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: Difficulty): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBatch(vararg items: Difficulty): List<Long>

    @Delete
    suspend fun delete(item: Difficulty): Int

    @Delete
    suspend fun deleteBatch(vararg items: Difficulty): Int
}
