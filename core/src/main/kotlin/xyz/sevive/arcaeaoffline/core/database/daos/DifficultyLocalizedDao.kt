package xyz.sevive.arcaeaoffline.core.database.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaScoreRatingClass
import xyz.sevive.arcaeaoffline.core.database.entities.DifficultyLocalized

@Dao
interface DifficultyLocalizedDao {
    @Query("SELECT * FROM difficulties_localized WHERE song_id = :songId AND rating_class = :ratingClass")
    fun find(songId: String, ratingClass: ArcaeaScoreRatingClass): Flow<DifficultyLocalized>

    @Query("SELECT * FROM difficulties_localized")
    fun findAll(): Flow<List<DifficultyLocalized>>

    @Query("SELECT * FROM difficulties_localized WHERE song_id = :songId")
    fun findAllBySongId(songId: String): Flow<List<DifficultyLocalized>>

    @Upsert
    suspend fun upsert(item: DifficultyLocalized)

    @Upsert
    suspend fun upsertAll(vararg items: DifficultyLocalized)

    @Delete
    suspend fun delete(item: DifficultyLocalized)

    @Delete
    suspend fun deleteAll(vararg items: DifficultyLocalized)
}

