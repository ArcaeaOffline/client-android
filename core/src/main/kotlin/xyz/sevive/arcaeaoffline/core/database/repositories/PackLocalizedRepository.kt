package xyz.sevive.arcaeaoffline.core.database.repositories

import kotlinx.coroutines.flow.Flow
import xyz.sevive.arcaeaoffline.core.database.daos.PackLocalizedDao
import xyz.sevive.arcaeaoffline.core.database.entities.PackLocalized

interface PackLocalizedRepository {
    fun find(id: String): Flow<PackLocalized?>
    fun findAll(): Flow<List<PackLocalized>>
    suspend fun insert(item: PackLocalized): Long
    suspend fun insertBatch(vararg items: PackLocalized): List<Long>
    suspend fun insertBatch(items: List<PackLocalized>) = insertBatch(*items.toTypedArray())
    suspend fun delete(item: PackLocalized): Int
    suspend fun deleteBatch(vararg items: PackLocalized): Int
}

class PackLocalizedRepositoryImpl(private val dao: PackLocalizedDao) : PackLocalizedRepository {
    override fun find(id: String): Flow<PackLocalized?> = dao.find(id)

    override fun findAll(): Flow<List<PackLocalized>> = dao.findAll()

    override suspend fun insert(item: PackLocalized) = dao.insert(item)

    override suspend fun insertBatch(vararg items: PackLocalized) = dao.insertBatch(*items)

    override suspend fun delete(item: PackLocalized) = dao.delete(item)

    override suspend fun deleteBatch(vararg items: PackLocalized) = dao.deleteBatch(*items)
}
