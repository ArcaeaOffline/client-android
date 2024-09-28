package xyz.sevive.arcaeaoffline.core.database.repositories

import kotlinx.coroutines.flow.Flow
import xyz.sevive.arcaeaoffline.core.database.daos.SongDao
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.core.database.entities.Song

interface SongRepository {
    fun find(id: String): Flow<Song?>
    fun find(playResult: PlayResult) = find(playResult.songId)
    fun findBySet(set: String): Flow<List<Song>>
    fun findAll(): Flow<List<Song>>
    suspend fun upsert(item: Song): Long
    suspend fun upsertBatch(vararg items: Song): List<Long>
    suspend fun delete(item: Song): Int
    suspend fun deleteBatch(vararg items: Song): Int
}


class SongRepositoryImpl(private val dao: SongDao) : SongRepository {
    override fun find(id: String): Flow<Song?> = dao.find(id)

    override fun findBySet(set: String): Flow<List<Song>> = dao.findBySet(set)

    override fun findAll(): Flow<List<Song>> = dao.findAll()

    override suspend fun upsert(item: Song) = dao.upsert(item)

    override suspend fun upsertBatch(vararg items: Song) = dao.upsertBatch(*items)

    override suspend fun delete(item: Song) = dao.delete(item)

    override suspend fun deleteBatch(vararg items: Song) = dao.deleteBatch(*items)
}
