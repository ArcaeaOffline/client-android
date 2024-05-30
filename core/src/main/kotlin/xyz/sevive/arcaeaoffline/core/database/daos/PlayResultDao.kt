package xyz.sevive.arcaeaoffline.core.database.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult


@Dao
interface PlayResultDao {
    @Query("SELECT * FROM scores WHERE song_id = :songId AND rating_class = :ratingClass")
    fun find(songId: String, ratingClass: ArcaeaRatingClass): Flow<PlayResult>

    @Query("SELECT * FROM scores")
    fun findAll(): Flow<List<PlayResult>>

    @Query("SELECT * FROM scores WHERE song_id = :songId")
    fun findAllBySongId(songId: String): Flow<List<PlayResult>>

    @Upsert
    suspend fun upsert(item: PlayResult)

    @Upsert
    suspend fun upsertAll(vararg items: PlayResult)

    @Delete
    suspend fun delete(item: PlayResult)

    @Delete
    suspend fun deleteAll(vararg items: PlayResult)
}


