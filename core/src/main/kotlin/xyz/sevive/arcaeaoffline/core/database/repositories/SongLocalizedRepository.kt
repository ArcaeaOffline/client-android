package xyz.sevive.arcaeaoffline.core.database.repositories

import kotlinx.coroutines.flow.Flow
import xyz.sevive.arcaeaoffline.core.database.daos.SongLocalizedDao
import xyz.sevive.arcaeaoffline.core.database.entities.SongLocalized

interface SongLocalizedRepository {
    fun find(id: String): Flow<SongLocalized?>
    fun findAll(): Flow<List<SongLocalized>>
    suspend fun upsert(item: SongLocalized)
    suspend fun upsertAll(vararg items: SongLocalized)
    suspend fun delete(item: SongLocalized)
    suspend fun deleteAll(vararg items: SongLocalized)
}

class SongLocalizedRepositoryImpl(private val dao: SongLocalizedDao) : SongLocalizedRepository {
    override fun find(id: String): Flow<SongLocalized?> = dao.find(id)

    override fun findAll(): Flow<List<SongLocalized>> = dao.findAll()

    override suspend fun upsert(item: SongLocalized) = dao.upsert(item)

    override suspend fun upsertAll(vararg items: SongLocalized) = dao.upsertAll(*items)

    override suspend fun delete(item: SongLocalized) = dao.delete(item)

    override suspend fun deleteAll(vararg items: SongLocalized) = dao.deleteAll(*items)
}


