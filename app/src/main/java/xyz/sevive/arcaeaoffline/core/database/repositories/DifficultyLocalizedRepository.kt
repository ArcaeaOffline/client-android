package xyz.sevive.arcaeaoffline.core.database.repositories

import kotlinx.coroutines.flow.Flow
import xyz.sevive.arcaeaoffline.core.database.daos.DifficultyLocalizedDao
import xyz.sevive.arcaeaoffline.core.database.entities.DifficultyLocalized

interface DifficultyLocalizedRepository {
    fun find(songId: String, ratingClass: Int): Flow<DifficultyLocalized?>
    fun findAll(): Flow<List<DifficultyLocalized>>
    fun findAllBySongId(songId: String): Flow<List<DifficultyLocalized>>
    suspend fun upsert(item: DifficultyLocalized)
    suspend fun upsertAll(vararg items: DifficultyLocalized)
    suspend fun delete(item: DifficultyLocalized)
    suspend fun deleteAll(vararg items: DifficultyLocalized)
}

class DifficultyLocalizedRepositoryImpl(private val dao: DifficultyLocalizedDao) :
    DifficultyLocalizedRepository {
    override fun find(songId: String, ratingClass: Int): Flow<DifficultyLocalized?> =
        dao.find(songId, ratingClass)

    override fun findAll(): Flow<List<DifficultyLocalized>> = dao.findAll()

    override fun findAllBySongId(songId: String): Flow<List<DifficultyLocalized>> =
        dao.findAllBySongId(songId)

    override suspend fun upsert(item: DifficultyLocalized) = dao.upsert(item)

    override suspend fun upsertAll(vararg items: DifficultyLocalized) = dao.upsertAll(*items)

    override suspend fun delete(item: DifficultyLocalized) = dao.delete(item)

    override suspend fun deleteAll(vararg items: DifficultyLocalized) = dao.deleteAll(*items)
}
