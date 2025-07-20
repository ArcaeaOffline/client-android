package xyz.sevive.arcaeaoffline.core.database.daos

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResultCalculated

@Dao
interface PlayResultCalculatedDao {
    @Query("SELECT * FROM play_results_calculated WHERE id = :id")
    fun find(id: Int): Flow<PlayResultCalculated?>

    @Query("SELECT * FROM play_results_calculated")
    fun findAll(): Flow<List<PlayResultCalculated>>

    @Query("SELECT * FROM play_results_calculated WHERE song_id = :songId")
    fun findAllBySongId(songId: String): Flow<List<PlayResultCalculated>>
}

