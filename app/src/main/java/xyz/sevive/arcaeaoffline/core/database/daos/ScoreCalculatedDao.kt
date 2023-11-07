package xyz.sevive.arcaeaoffline.core.database.daos

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import xyz.sevive.arcaeaoffline.core.database.entities.ScoreCalculated

@Dao
interface ScoreCalculatedDao {
    @Query("SELECT * FROM scores_calculated WHERE song_id = :songId AND rating_class = :ratingClass")
    fun find(songId: String, ratingClass: Int): Flow<ScoreCalculated>

    @Query("SELECT * FROM scores_calculated")
    fun findAll(): Flow<List<ScoreCalculated>>

    @Query("SELECT * FROM scores_calculated WHERE song_id = :songId")
    fun findAllBySongId(songId: String): Flow<List<ScoreCalculated>>
}

