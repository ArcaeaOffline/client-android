package xyz.sevive.arcaeaoffline.core.database.repositories

import kotlinx.coroutines.flow.Flow
import xyz.sevive.arcaeaoffline.core.database.daos.PackLocalizedDao
import xyz.sevive.arcaeaoffline.core.database.entities.PackLocalized

interface PackLocalizedRepository {
    fun find(id: String): Flow<PackLocalized?>
    fun findAll(): Flow<List<PackLocalized>>
    suspend fun insert(item: PackLocalized): Long
    suspend fun insertAll(vararg items: PackLocalized): List<Long>
    suspend fun insertAll(items: List<PackLocalized>) = insertAll(*items.toTypedArray())
    suspend fun delete(item: PackLocalized): Int
    suspend fun deleteAll(vararg items: PackLocalized): Int
}

class PackLocalizedRepositoryImpl(private val dao: PackLocalizedDao) : PackLocalizedRepository {
    override fun find(id: String): Flow<PackLocalized?> = dao.find(id)

    override fun findAll(): Flow<List<PackLocalized>> = dao.findAll()

    override suspend fun insert(item: PackLocalized) = dao.insert(item)

    override suspend fun insertAll(vararg items: PackLocalized) = dao.insertAll(*items)

    override suspend fun delete(item: PackLocalized) = dao.delete(item)

    override suspend fun deleteAll(vararg items: PackLocalized) = dao.deleteAll(*items)
}
