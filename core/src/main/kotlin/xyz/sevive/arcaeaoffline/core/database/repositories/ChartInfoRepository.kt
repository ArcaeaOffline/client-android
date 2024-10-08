package xyz.sevive.arcaeaoffline.core.database.repositories

import kotlinx.coroutines.flow.Flow
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.database.daos.ChartInfoDao
import xyz.sevive.arcaeaoffline.core.database.entities.ChartInfo
import xyz.sevive.arcaeaoffline.core.database.entities.Difficulty
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult

interface ChartInfoRepository {
    fun find(songId: String, ratingClass: ArcaeaRatingClass): Flow<ChartInfo?>
    fun find(playResult: PlayResult): Flow<ChartInfo?> =
        find(playResult.songId, playResult.ratingClass)

    fun find(difficulty: Difficulty): Flow<ChartInfo?> =
        find(difficulty.songId, difficulty.ratingClass)

    fun findAll(): Flow<List<ChartInfo>>
    fun findAllBySongId(songId: String): Flow<List<ChartInfo>>
    fun count(): Flow<Int>
    suspend fun insert(item: ChartInfo)
    suspend fun insertBatch(vararg items: ChartInfo): LongArray
    suspend fun delete(item: ChartInfo)
    suspend fun deleteBatch(vararg items: ChartInfo)
}

class ChartInfoRepositoryImpl(private val dao: ChartInfoDao) : ChartInfoRepository {
    override fun find(songId: String, ratingClass: ArcaeaRatingClass): Flow<ChartInfo?> =
        dao.find(songId, ratingClass)

    override fun findAll(): Flow<List<ChartInfo>> = dao.findAll()

    override fun findAllBySongId(songId: String): Flow<List<ChartInfo>> =
        dao.findAllBySongId(songId)

    override fun count(): Flow<Int> = dao.count()

    override suspend fun insert(item: ChartInfo) = dao.insert(item)

    override suspend fun insertBatch(vararg items: ChartInfo): LongArray = dao.insertBatch(*items)

    override suspend fun delete(item: ChartInfo) = dao.delete(item)

    override suspend fun deleteBatch(vararg items: ChartInfo) = dao.deleteBatch(*items)
}
