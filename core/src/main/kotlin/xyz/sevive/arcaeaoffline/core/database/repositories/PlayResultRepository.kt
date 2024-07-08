package xyz.sevive.arcaeaoffline.core.database.repositories

import kotlinx.coroutines.flow.Flow
import org.threeten.bp.Instant
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.database.daos.PlayResultDao
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import java.util.UUID


interface PlayResultRepository {
    fun find(songId: String, ratingClass: ArcaeaRatingClass): Flow<PlayResult?>
    fun findByUUID(uuid: UUID): Flow<PlayResult>
    fun findLaterThan(date: Instant): Flow<List<PlayResult>>
    fun findAll(): Flow<List<PlayResult>>
    fun findAllBySongId(songId: String): Flow<List<PlayResult>>
    suspend fun upsert(item: PlayResult)
    suspend fun upsertAll(vararg items: PlayResult)
    suspend fun delete(item: PlayResult)
    suspend fun deleteAll(vararg items: PlayResult)
}

class PlayResultRepositoryImpl(val dao: PlayResultDao) : PlayResultRepository {
    override fun find(songId: String, ratingClass: ArcaeaRatingClass): Flow<PlayResult?> =
        dao.find(songId, ratingClass)

    override fun findByUUID(uuid: UUID): Flow<PlayResult> = dao.findByUUID(uuid)

    override fun findLaterThan(date: Instant): Flow<List<PlayResult>> = dao.findLaterThan(date)

    override fun findAll(): Flow<List<PlayResult>> = dao.findAll()

    override fun findAllBySongId(songId: String): Flow<List<PlayResult>> =
        dao.findAllBySongId(songId)

    override suspend fun upsert(item: PlayResult) = dao.upsert(item)

    override suspend fun upsertAll(vararg items: PlayResult) = dao.upsertAll(*items)

    override suspend fun delete(item: PlayResult) = dao.delete(item)

    override suspend fun deleteAll(vararg items: PlayResult) = dao.deleteAll(*items)
}
