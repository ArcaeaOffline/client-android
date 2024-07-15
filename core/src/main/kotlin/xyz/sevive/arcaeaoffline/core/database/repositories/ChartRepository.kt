package xyz.sevive.arcaeaoffline.core.database.repositories

import kotlinx.coroutines.flow.Flow
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.database.daos.ChartDao
import xyz.sevive.arcaeaoffline.core.database.entities.Chart
import xyz.sevive.arcaeaoffline.core.database.entities.Difficulty
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.core.database.entities.Song

interface ChartRepository {
    fun find(songId: String, ratingClass: ArcaeaRatingClass): Flow<Chart?>
    fun find(playResult: PlayResult): Flow<Chart?> = find(playResult.songId, playResult.ratingClass)
    fun find(difficulty: Difficulty): Flow<Chart?> = find(difficulty.songId, difficulty.ratingClass)
    fun findAll(): Flow<List<Chart>>
    fun findAllBySongId(songId: String): Flow<List<Chart>>
    fun findAllBySongId(song: Song): Flow<List<Chart>> = findAllBySongId(song.id)
}

class ChartRepositoryImpl(private val dao: ChartDao) : ChartRepository {
    override fun find(songId: String, ratingClass: ArcaeaRatingClass): Flow<Chart?> =
        dao.find(songId, ratingClass)

    override fun findAll(): Flow<List<Chart>> = dao.findAll()
    override fun findAllBySongId(songId: String): Flow<List<Chart>> = dao.findAllBySongId(songId)
}
