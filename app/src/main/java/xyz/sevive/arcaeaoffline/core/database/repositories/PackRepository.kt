package xyz.sevive.arcaeaoffline.core.database.repositories

import kotlinx.coroutines.flow.Flow
import xyz.sevive.arcaeaoffline.core.database.daos.PackDao
import xyz.sevive.arcaeaoffline.core.database.entities.Pack

interface PackRepository {
    fun find(id: String): Flow<Pack?>
    fun findAll(): Flow<List<Pack>>
    suspend fun upsert(item: Pack)
    suspend fun upsertAll(vararg items: Pack)
    suspend fun delete(item: Pack)
    suspend fun deleteAll(vararg items: Pack)
}

class PackRepositoryImpl(private val dao: PackDao) : PackRepository {
    override fun find(id: String): Flow<Pack?> = dao.find(id)

    override fun findAll(): Flow<List<Pack>> = dao.findAll()

    override suspend fun upsert(item: Pack) = dao.upsert(item)

    override suspend fun upsertAll(vararg items: Pack) = dao.upsertAll(*items)

    override suspend fun delete(item: Pack) = dao.delete(item)

    override suspend fun deleteAll(vararg items: Pack) = dao.deleteAll(*items)
}


