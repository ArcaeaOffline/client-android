package xyz.sevive.arcaeaoffline.core.database.repositories

import kotlinx.coroutines.flow.Flow
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.database.daos.PlayResultBestDao
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResultBest

interface PlayResultBestRepository {
    fun find(songId: String, ratingClass: ArcaeaRatingClass): Flow<PlayResultBest?>
    fun findAll(): Flow<List<PlayResultBest>>
    fun findAllBySongId(songId: String): Flow<List<PlayResultBest>>
    fun listDescWithLimit(limit: Int): Flow<List<PlayResultBest>>
}

class PlayResultBestRepositoryImpl(private val dao: PlayResultBestDao) : PlayResultBestRepository {
    override fun find(songId: String, ratingClass: ArcaeaRatingClass): Flow<PlayResultBest?> =
        dao.find(songId, ratingClass)

    override fun findAll(): Flow<List<PlayResultBest>> = dao.findAll()

    override fun findAllBySongId(songId: String): Flow<List<PlayResultBest>> =
        dao.findAllBySongId(songId)

    override fun listDescWithLimit(limit: Int): Flow<List<PlayResultBest>> =
        dao.listDescWithLimit(limit)
}

