package xyz.sevive.arcaeaoffline.core.database.repositories

import kotlinx.coroutines.flow.Flow
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaScoreRatingClass
import xyz.sevive.arcaeaoffline.core.database.daos.ChartInfoDao
import xyz.sevive.arcaeaoffline.core.database.entities.ChartInfo

interface ChartInfoRepository {
    fun find(songId: String, ratingClass: ArcaeaScoreRatingClass): Flow<ChartInfo?>
    fun findAll(): Flow<List<ChartInfo>>
    fun findAllBySongId(songId: String): Flow<List<ChartInfo>>
    suspend fun insert(item: ChartInfo)
    suspend fun insertAll(vararg items: ChartInfo): LongArray
    suspend fun delete(item: ChartInfo)
    suspend fun deleteAll(vararg items: ChartInfo)
}

class ChartInfoRepositoryImpl(private val dao: ChartInfoDao) : ChartInfoRepository {
    override fun find(songId: String, ratingClass: ArcaeaScoreRatingClass): Flow<ChartInfo?> =
        dao.find(songId, ratingClass)

    override fun findAll(): Flow<List<ChartInfo>> = dao.findAll()

    override fun findAllBySongId(songId: String): Flow<List<ChartInfo>> =
        dao.findAllBySongId(songId)

    override suspend fun insert(item: ChartInfo) = dao.insert(item)

    override suspend fun insertAll(vararg items: ChartInfo): LongArray = dao.insertAll(*items)

    override suspend fun delete(item: ChartInfo) = dao.delete(item)

    override suspend fun deleteAll(vararg items: ChartInfo) = dao.deleteAll(*items)
}
