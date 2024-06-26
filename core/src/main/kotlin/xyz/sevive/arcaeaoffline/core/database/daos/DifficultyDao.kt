package xyz.sevive.arcaeaoffline.core.database.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
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

    @Upsert
    suspend fun upsert(item: Difficulty)

    @Upsert
    suspend fun upsertAll(vararg items: Difficulty): LongArray

    @Delete
    suspend fun delete(item: Difficulty)

    @Delete
    suspend fun deleteAll(vararg items: Difficulty)
}
