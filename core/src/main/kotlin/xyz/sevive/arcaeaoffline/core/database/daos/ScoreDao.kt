package xyz.sevive.arcaeaoffline.core.database.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaScoreRatingClass
import xyz.sevive.arcaeaoffline.core.database.entities.Score


@Dao
interface ScoreDao {
    @Query("SELECT * FROM scores WHERE song_id = :songId AND rating_class = :ratingClass")
    fun find(songId: String, ratingClass: ArcaeaScoreRatingClass): Flow<Score>

    @Query("SELECT * FROM scores")
    fun findAll(): Flow<List<Score>>

    @Query("SELECT * FROM scores WHERE song_id = :songId")
    fun findAllBySongId(songId: String): Flow<List<Score>>

    @Upsert
    suspend fun upsert(item: Score)

    @Upsert
    suspend fun upsertAll(vararg items: Score)

    @Delete
    suspend fun delete(item: Score)

    @Delete
    suspend fun deleteAll(vararg items: Score)
}


