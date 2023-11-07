package xyz.sevive.arcaeaoffline.core.database.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import xyz.sevive.arcaeaoffline.core.database.entities.ChartInfo


@Dao
interface ChartInfoDao {
    @Query("SELECT * FROM charts_info WHERE song_id = :songId AND rating_class = :ratingClass")
    fun find(songId: String, ratingClass: Int): Flow<ChartInfo>

    @Query("SELECT * FROM charts_info")
    fun findAll(): Flow<List<ChartInfo>>

    @Query("SELECT * FROM charts_info WHERE song_id = :songId")
    fun findAllBySongId(songId: String): Flow<List<ChartInfo>>

    @Upsert
    suspend fun upsert(item: ChartInfo)

    @Upsert
    suspend fun upsertAll(vararg items: ChartInfo)

    @Delete
    suspend fun delete(item: ChartInfo)

    @Delete
    suspend fun deleteAll(vararg items: ChartInfo)
}

