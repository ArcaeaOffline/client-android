package xyz.sevive.arcaeaoffline.core.database.repositories

import kotlinx.coroutines.flow.Flow
import xyz.sevive.arcaeaoffline.core.database.daos.SongDao
import xyz.sevive.arcaeaoffline.core.database.entities.Song

interface SongRepository {
    fun find(id: String): Flow<Song?>
    fun findBySet(set: String): Flow<List<Song>>
    fun findAll(): Flow<List<Song>>
    suspend fun upsert(item: Song)
    suspend fun upsertAll(vararg items: Song): LongArray
    suspend fun delete(item: Song)
    suspend fun deleteAll(vararg items: Song)
}


class SongRepositoryImpl(private val dao: SongDao) : SongRepository {
    override fun find(id: String): Flow<Song?> = dao.find(id)

    override fun findBySet(set: String): Flow<List<Song>> = dao.findBySet(set)

    override fun findAll(): Flow<List<Song>> = dao.findAll()

    override suspend fun upsert(item: Song) = dao.upsert(item)

    override suspend fun upsertAll(vararg items: Song) = dao.upsertAll(*items)

    override suspend fun delete(item: Song) = dao.delete(item)

    override suspend fun deleteAll(vararg items: Song) = dao.deleteAll(*items)
}
