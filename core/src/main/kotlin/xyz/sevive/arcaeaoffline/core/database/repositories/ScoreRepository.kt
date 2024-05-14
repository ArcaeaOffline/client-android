package xyz.sevive.arcaeaoffline.core.database.repositories

import kotlinx.coroutines.flow.Flow
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaScoreRatingClass
import xyz.sevive.arcaeaoffline.core.database.daos.ScoreDao
import xyz.sevive.arcaeaoffline.core.database.entities.Score


interface ScoreRepository {
    fun find(songId: String, ratingClass: ArcaeaScoreRatingClass): Flow<Score?>
    fun findAll(): Flow<List<Score>>
    fun findAllBySongId(songId: String): Flow<List<Score>>
    suspend fun upsert(item: Score)
    suspend fun upsertAll(vararg items: Score)
    suspend fun delete(item: Score)
    suspend fun deleteAll(vararg items: Score)
}

class ScoreRepositoryImpl(val dao: ScoreDao) : ScoreRepository {
    override fun find(songId: String, ratingClass: ArcaeaScoreRatingClass): Flow<Score?> =
        dao.find(songId, ratingClass)

    override fun findAll(): Flow<List<Score>> = dao.findAll()

    override fun findAllBySongId(songId: String): Flow<List<Score>> = dao.findAllBySongId(songId)

    override suspend fun upsert(item: Score) = dao.upsert(item)

    override suspend fun upsertAll(vararg items: Score) = dao.upsertAll(*items)

    override suspend fun delete(item: Score) = dao.delete(item)

    override suspend fun deleteAll(vararg items: Score) = dao.deleteAll(*items)
}
