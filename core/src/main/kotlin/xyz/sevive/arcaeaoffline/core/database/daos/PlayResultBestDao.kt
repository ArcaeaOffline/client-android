package xyz.sevive.arcaeaoffline.core.database.daos

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResultBest

@Dao
interface PlayResultBestDao {
    @Query("SELECT * FROM play_results_best WHERE song_id = :songId AND rating_class = :ratingClass")
    fun find(songId: String, ratingClass: ArcaeaRatingClass): Flow<PlayResultBest>

    @Query("SELECT * FROM play_results_best")
    fun findAll(): Flow<List<PlayResultBest>>

    @Query("SELECT * FROM play_results_best WHERE song_id = :songId")
    fun findAllBySongId(songId: String): Flow<List<PlayResultBest>>

    @Query("SELECT * FROM play_results_best ORDER BY potential DESC LIMIT :limit")
    fun orderDescWithLimit(limit: Int): Flow<List<PlayResultBest>>
}

