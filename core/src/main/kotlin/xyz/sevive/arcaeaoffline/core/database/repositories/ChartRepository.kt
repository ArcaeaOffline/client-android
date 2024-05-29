package xyz.sevive.arcaeaoffline.core.database.repositories

import kotlinx.coroutines.flow.Flow
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.database.daos.ChartDao
import xyz.sevive.arcaeaoffline.core.database.entities.Chart

interface ChartRepository {
    fun find(songId: String, ratingClass: ArcaeaRatingClass): Flow<Chart?>
    fun findAll(): Flow<List<Chart>>
    fun findAllBySongId(songId: String): Flow<List<Chart>>
}

class ChartRepositoryImpl(private val dao: ChartDao) : ChartRepository {
    override fun find(songId: String, ratingClass: ArcaeaRatingClass): Flow<Chart?> =
        dao.find(songId, ratingClass)

    override fun findAll(): Flow<List<Chart>> = dao.findAll()
    override fun findAllBySongId(songId: String): Flow<List<Chart>> = dao.findAllBySongId(songId)
}
