package xyz.sevive.arcaeaoffline.core.database.repositories

import kotlinx.coroutines.flow.Flow
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.database.daos.ScoreBestDao
import xyz.sevive.arcaeaoffline.core.database.entities.ScoreBest

interface ScoreBestRepository {
    fun find(songId: String, ratingClass: ArcaeaRatingClass): Flow<ScoreBest?>
    fun findAll(): Flow<List<ScoreBest>>
    fun findAllBySongId(songId: String): Flow<List<ScoreBest>>
    fun listDescWithLimit(limit: Int): Flow<List<ScoreBest>>
}

class ScoreBestRepositoryImpl(private val dao: ScoreBestDao) : ScoreBestRepository {
    override fun find(songId: String, ratingClass: ArcaeaRatingClass): Flow<ScoreBest?> =
        dao.find(songId, ratingClass)

    override fun findAll(): Flow<List<ScoreBest>> = dao.findAll()

    override fun findAllBySongId(songId: String): Flow<List<ScoreBest>> =
        dao.findAllBySongId(songId)

    override fun listDescWithLimit(limit: Int): Flow<List<ScoreBest>> = dao.listDescWithLimit(limit)
}

