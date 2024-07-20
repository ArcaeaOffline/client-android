package xyz.sevive.arcaeaoffline.core.database.repositories

import kotlinx.coroutines.flow.Flow
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.database.daos.DifficultyDao
import xyz.sevive.arcaeaoffline.core.database.entities.ChartInfo
import xyz.sevive.arcaeaoffline.core.database.entities.Difficulty
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult

interface DifficultyRepository {
    fun find(songId: String, ratingClass: ArcaeaRatingClass): Flow<Difficulty?>
    fun find(playResult: PlayResult) = find(playResult.songId, playResult.ratingClass)
    fun find(chartInfo: ChartInfo) = find(chartInfo.songId, chartInfo.ratingClass)
    fun findAll(): Flow<List<Difficulty>>
    fun findAllBySongId(songId: String): Flow<List<Difficulty>>
    suspend fun upsert(item: Difficulty)
    suspend fun upsertAll(vararg items: Difficulty): LongArray
    suspend fun delete(item: Difficulty)
    suspend fun deleteAll(vararg items: Difficulty)
}

class DifficultyRepositoryImpl(private val dao: DifficultyDao) : DifficultyRepository {
    override fun find(songId: String, ratingClass: ArcaeaRatingClass): Flow<Difficulty?> =
        dao.find(songId, ratingClass)

    override fun findAll(): Flow<List<Difficulty>> = dao.findAll()

    override fun findAllBySongId(songId: String): Flow<List<Difficulty>> =
        dao.findAllBySongId(songId)

    override suspend fun upsert(item: Difficulty) = dao.upsert(item)

    override suspend fun upsertAll(vararg items: Difficulty) = dao.upsertAll(*items)

    override suspend fun delete(item: Difficulty) = dao.delete(item)

    override suspend fun deleteAll(vararg items: Difficulty) = dao.deleteAll(*items)
}
