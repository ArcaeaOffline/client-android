package xyz.sevive.arcaeaoffline.core.database.repositories

import kotlinx.coroutines.flow.Flow
import xyz.sevive.arcaeaoffline.core.database.daos.SongLocalizedDao
import xyz.sevive.arcaeaoffline.core.database.entities.SongLocalized

interface SongLocalizedRepository {
    fun find(id: String): Flow<SongLocalized?>
    fun findAll(): Flow<List<SongLocalized>>
    suspend fun insert(item: SongLocalized): Long
    suspend fun insertAll(vararg items: SongLocalized): List<Long>
    suspend fun insertAll(items: List<SongLocalized>) = insertAll(*items.toTypedArray())
    suspend fun delete(item: SongLocalized): Int
    suspend fun deleteAll(vararg items: SongLocalized): Int
}

class SongLocalizedRepositoryImpl(private val dao: SongLocalizedDao) : SongLocalizedRepository {
    override fun find(id: String): Flow<SongLocalized?> = dao.find(id)

    override fun findAll(): Flow<List<SongLocalized>> = dao.findAll()

    override suspend fun insert(item: SongLocalized) = dao.insert(item)

    override suspend fun insertAll(vararg items: SongLocalized) = dao.insertAll(*items)

    override suspend fun delete(item: SongLocalized) = dao.delete(item)

    override suspend fun deleteAll(vararg items: SongLocalized) = dao.deleteAll(*items)
}
