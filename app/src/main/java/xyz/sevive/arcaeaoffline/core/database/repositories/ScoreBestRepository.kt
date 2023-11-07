package xyz.sevive.arcaeaoffline.core.database.repositories

import kotlinx.coroutines.flow.Flow
import xyz.sevive.arcaeaoffline.core.database.daos.ScoreBestDao
import xyz.sevive.arcaeaoffline.core.database.entities.ScoreBest

interface ScoreBestRepository {
    fun find(songId: String, ratingClass: Int): Flow<ScoreBest?>
    fun findAll(): Flow<List<ScoreBest>>
    fun findAllBySongId(songId: String): Flow<List<ScoreBest>>
}

class ScoreBestRepositoryImpl(private val dao: ScoreBestDao) : ScoreBestRepository {
    override fun find(songId: String, ratingClass: Int): Flow<ScoreBest?> =
        dao.find(songId, ratingClass)

    override fun findAll(): Flow<List<ScoreBest>> = dao.findAll()

    override fun findAllBySongId(songId: String): Flow<List<ScoreBest>> =
        dao.findAllBySongId(songId)
}

