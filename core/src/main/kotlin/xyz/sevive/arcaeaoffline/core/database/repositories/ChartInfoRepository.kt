package xyz.sevive.arcaeaoffline.core.database.repositories

import kotlinx.coroutines.flow.Flow
import xyz.sevive.arcaeaoffline.core.database.daos.ChartInfoDao
import xyz.sevive.arcaeaoffline.core.database.entities.ChartInfo

interface ChartInfoRepository {
    fun find(songId: String, ratingClass: Int): Flow<ChartInfo?>
    fun findAll(): Flow<List<ChartInfo>>
    fun findAllBySongId(songId: String): Flow<List<ChartInfo>>
    suspend fun upsert(item: ChartInfo)
    suspend fun upsertAll(vararg items: ChartInfo)
    suspend fun delete(item: ChartInfo)
    suspend fun deleteAll(vararg items: ChartInfo)
}

class ChartInfoRepositoryImpl(private val dao: ChartInfoDao) : ChartInfoRepository {
    override fun find(songId: String, ratingClass: Int): Flow<ChartInfo?> =
        dao.find(songId, ratingClass)

    override fun findAll(): Flow<List<ChartInfo>> = dao.findAll()

    override fun findAllBySongId(songId: String): Flow<List<ChartInfo>> =
        dao.findAllBySongId(songId)

    override suspend fun upsert(item: ChartInfo) = dao.upsert(item)

    override suspend fun upsertAll(vararg items: ChartInfo) = dao.upsertAll(*items)

    override suspend fun delete(item: ChartInfo) = dao.delete(item)

    override suspend fun deleteAll(vararg items: ChartInfo) = dao.deleteAll(*items)
}
