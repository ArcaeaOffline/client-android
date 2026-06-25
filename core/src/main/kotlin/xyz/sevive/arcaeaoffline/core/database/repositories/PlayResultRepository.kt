package xyz.sevive.arcaeaoffline.core.database.repositories

import kotlinx.coroutines.flow.Flow
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.database.daos.PlayResultDao
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import kotlin.time.Instant
import kotlin.uuid.Uuid

interface PlayResultRepository {
    fun find(
        songId: String,
        ratingClass: ArcaeaRatingClass,
    ): Flow<PlayResult?>

    fun findByUuid(uuid: Uuid): Flow<PlayResult?>

    fun findLaterThan(date: Instant): Flow<List<PlayResult>>

    fun findAll(): Flow<List<PlayResult>>

    fun findAllBySongId(songId: String): Flow<List<PlayResult>>

    fun findAllByUuid(uuids: List<Uuid>): Flow<List<PlayResult>>

    fun count(): Flow<Int>

    suspend fun upsert(item: PlayResult): Long

    suspend fun upsertBatch(vararg items: PlayResult): List<Long>

    suspend fun delete(item: PlayResult): Int

    suspend fun deleteBatch(vararg items: PlayResult): Int
}

class PlayResultRepositoryImpl(
    val dao: PlayResultDao,
) : PlayResultRepository {
    override fun find(
        songId: String,
        ratingClass: ArcaeaRatingClass,
    ): Flow<PlayResult?> = dao.find(songId, ratingClass)

    override fun findByUuid(uuid: Uuid): Flow<PlayResult?> = dao.findByUuid(uuid)

    override fun findLaterThan(date: Instant): Flow<List<PlayResult>> = dao.findLaterThan(date)

    override fun findAll(): Flow<List<PlayResult>> = dao.findAll()

    override fun findAllBySongId(songId: String): Flow<List<PlayResult>> = dao.findAllBySongId(songId)

    override fun findAllByUuid(uuids: List<Uuid>): Flow<List<PlayResult>> = dao.findAllByUuid(uuids)

    override fun count(): Flow<Int> = dao.count()

    override suspend fun upsert(item: PlayResult) = dao.upsert(item)

    override suspend fun upsertBatch(vararg items: PlayResult) = dao.upsertBatch(*items)

    override suspend fun delete(item: PlayResult) = dao.delete(item)

    override suspend fun deleteBatch(vararg items: PlayResult) = dao.deleteBatch(*items)
}
