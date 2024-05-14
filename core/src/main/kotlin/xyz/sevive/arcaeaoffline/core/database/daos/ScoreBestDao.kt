package xyz.sevive.arcaeaoffline.core.database.daos

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import xyz.sevive.arcaeaoffline.core.database.entities.ScoreBest

@Dao
interface ScoreBestDao {
    @Query("SELECT * FROM scores_best WHERE song_id = :songId AND rating_class = :ratingClass")
    fun find(songId: String, ratingClass: Int): Flow<ScoreBest>

    @Query("SELECT * FROM scores_best")
    fun findAll(): Flow<List<ScoreBest>>

    @Query("SELECT * FROM scores_best WHERE song_id = :songId")
    fun findAllBySongId(songId: String): Flow<List<ScoreBest>>

    @Query("SELECT * FROM scores_best ORDER BY potential DESC LIMIT :limit")
    fun listDescWithLimit(limit: Int): Flow<List<ScoreBest>>
}

