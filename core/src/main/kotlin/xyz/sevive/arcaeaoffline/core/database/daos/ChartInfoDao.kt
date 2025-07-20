package xyz.sevive.arcaeaoffline.core.database.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.database.entities.ChartInfo


@Dao
interface ChartInfoDao {
    @Query("SELECT * FROM charts_info WHERE song_id = :songId AND rating_class = :ratingClass")
    fun find(songId: String, ratingClass: ArcaeaRatingClass): Flow<ChartInfo?>

    @Query("SELECT * FROM charts_info")
    fun findAll(): Flow<List<ChartInfo>>

    @Query("SELECT * FROM charts_info WHERE song_id = :songId")
    fun findAllBySongId(songId: String): Flow<List<ChartInfo>>

    @Query("SELECT COUNT(*) FROM charts_info")
    fun count(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ChartInfo)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(vararg items: ChartInfo): LongArray

    @Delete
    suspend fun delete(item: ChartInfo)

    @Delete
    suspend fun deleteBatch(vararg items: ChartInfo)
}

