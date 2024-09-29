package xyz.sevive.arcaeaoffline.core.database.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import xyz.sevive.arcaeaoffline.core.database.daos.PackDao
import xyz.sevive.arcaeaoffline.core.database.entities.Pack

interface PackRepository {
    fun find(id: String): Flow<Pack?>
    fun findAll(): Flow<List<Pack>>
    fun findBasePack(id: String): Flow<Pack?>
    fun count(): Flow<Int>
    suspend fun upsert(item: Pack): Long
    suspend fun upsertBatch(vararg items: Pack): List<Long>
    suspend fun delete(item: Pack): Int
    suspend fun deleteBatch(vararg items: Pack): Int
}

class PackRepositoryImpl(private val dao: PackDao) : PackRepository {
    override fun find(id: String): Flow<Pack?> = dao.find(id)

    override fun findAll(): Flow<List<Pack>> = dao.findAll()

    override fun findBasePack(id: String): Flow<Pack?> {
        val re = """_append_.*$""".toRegex()
        return if (re.find(id) != null) {
            val baseId = re.replace(id, "")
            dao.find(baseId)
        } else MutableStateFlow(null).asStateFlow()
    }

    override fun count() = dao.count()

    override suspend fun upsert(item: Pack) = dao.upsert(item)

    override suspend fun upsertBatch(vararg items: Pack) = dao.upsertBatch(*items)

    override suspend fun delete(item: Pack) = dao.delete(item)

    override suspend fun deleteBatch(vararg items: Pack) = dao.deleteBatch(*items)
}


