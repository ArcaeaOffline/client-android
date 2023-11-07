package xyz.sevive.arcaeaoffline.core.database.repositories

import kotlinx.coroutines.flow.Flow
import xyz.sevive.arcaeaoffline.core.database.daos.PackLocalizedDao
import xyz.sevive.arcaeaoffline.core.database.entities.PackLocalized

interface PackLocalizedRepository {
    fun find(id: String): Flow<PackLocalized?>
    fun findAll(): Flow<List<PackLocalized>>
    suspend fun upsert(item: PackLocalized)
    suspend fun upsertAll(vararg items: PackLocalized)
    suspend fun delete(item: PackLocalized)
    suspend fun deleteAll(vararg items: PackLocalized)
}

class PackLocalizedRepositoryImpl(private val dao: PackLocalizedDao) : PackLocalizedRepository {
    override fun find(id: String): Flow<PackLocalized?> = dao.find(id)

    override fun findAll(): Flow<List<PackLocalized>> = dao.findAll()

    override suspend fun upsert(item: PackLocalized) = dao.upsert(item)

    override suspend fun upsertAll(vararg items: PackLocalized) = dao.upsertAll(*items)

    override suspend fun delete(item: PackLocalized) = dao.delete(item)

    override suspend fun deleteAll(vararg items: PackLocalized) = dao.deleteAll(*items)

}
