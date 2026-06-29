package xyz.sevive.arcaeaoffline.database.repositories

import android.net.Uri
import androidx.room.Transactor
import androidx.room.useWriterConnection
import kotlinx.coroutines.flow.Flow
import xyz.sevive.arcaeaoffline.core.database.ArcaeaOfflineDatabase
import xyz.sevive.arcaeaoffline.core.database.entities.Chart
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.core.database.repositories.PlayResultRepository
import xyz.sevive.arcaeaoffline.database.OcrQueueDatabase
import xyz.sevive.arcaeaoffline.database.entities.OcrQueueTask
import xyz.sevive.arcaeaoffline.database.entities.OcrQueueTaskStatus
import kotlin.time.Clock

interface OcrQueueTaskRepository {
    fun findAll(): Flow<List<OcrQueueTask>>

    suspend fun findById(id: Long): OcrQueueTask?

    fun findByStatus(statuses: List<OcrQueueTaskStatus>): Flow<List<OcrQueueTask>>

    fun findByStatus(vararg status: OcrQueueTaskStatus) = findByStatus(status.asList())

    fun count(): Flow<Int>

    fun countByStatus(statuses: List<OcrQueueTaskStatus>): Flow<Int>

    suspend fun insert(item: OcrQueueTask): Long

    suspend fun insertBatch(uris: List<Uri>): List<Long>

    suspend fun update(item: OcrQueueTask): Int

    suspend fun updateChart(
        id: Long,
        chart: Chart,
    ): Int?

    suspend fun updatePlayResult(
        id: Long,
        playResult: PlayResult,
    ): Int?

    suspend fun delete(item: OcrQueueTask): Int

    suspend fun delete(id: Long): Int

    suspend fun deleteBatch(items: List<OcrQueueTask>): Int

    suspend fun deleteAll()

    suspend fun save(item: OcrQueueTask)

    suspend fun save(itemId: Long)

    suspend fun saveBatch(items: List<OcrQueueTask>)
}

class OcrQueueTaskRepositoryImpl(
    private val ocrQueueDatabase: OcrQueueDatabase,
    private val arcaeaOfflineDatabase: ArcaeaOfflineDatabase,
    private val playResultRepository: PlayResultRepository,
) : OcrQueueTaskRepository {
    private val dao = ocrQueueDatabase.ocrQueueTaskDao()

    override fun findAll(): Flow<List<OcrQueueTask>> = dao.findAll()

    override suspend fun findById(id: Long): OcrQueueTask? = dao.findById(id)

    override fun findByStatus(statuses: List<OcrQueueTaskStatus>): Flow<List<OcrQueueTask>> = dao.findByStatus(statuses)

    override fun count(): Flow<Int> = dao.count()

    override fun countByStatus(statuses: List<OcrQueueTaskStatus>): Flow<Int> = dao.countByStatus(statuses)

    override suspend fun insert(item: OcrQueueTask): Long = dao.insert(item)

    override suspend fun insertBatch(uris: List<Uri>): List<Long> {
        val now = Clock.System.now()
        return dao.insertBatch(uris.map { OcrQueueTask(fileUri = it, insertedAt = now) })
    }

    override suspend fun update(item: OcrQueueTask): Int = dao.update(item)

    override suspend fun updateChart(
        id: Long,
        chart: Chart,
    ): Int? {
        var item = findById(id) ?: return null

        if (item.status == OcrQueueTaskStatus.ERROR) {
            item = item.copy(status = OcrQueueTaskStatus.DONE, errorType = null, errorMessage = null)
        }

        val newPlayResult =
            item.playResult?.copy(songId = chart.songId, ratingClass = chart.ratingClass)
                ?: PlayResult(songId = chart.songId, ratingClass = chart.ratingClass, score = 0)

        return update(
            item.copy(playResult = newPlayResult),
        )
    }

    override suspend fun updatePlayResult(
        id: Long,
        playResult: PlayResult,
    ): Int? {
        val item = findById(id) ?: return null
        return update(item.copy(playResult = playResult))
    }

    override suspend fun delete(item: OcrQueueTask): Int = dao.delete(item)

    override suspend fun delete(id: Long): Int = dao.delete(id)

    override suspend fun deleteBatch(items: List<OcrQueueTask>): Int = dao.deleteBatch(items)

    override suspend fun deleteAll() = dao.deleteAll()

    override suspend fun save(item: OcrQueueTask) {
        if (item.playResult == null) return

        ocrQueueDatabase.useWriterConnection { transactor ->
            arcaeaOfflineDatabase.useWriterConnection { aoTransactor ->
                transactor.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                    aoTransactor.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                        playResultRepository.upsert(item.playResult)
                        delete(item)
                    }
                }
            }
        }
    }

    override suspend fun save(itemId: Long) {
        dao.findById(itemId)?.let { save(it) }
    }

    override suspend fun saveBatch(items: List<OcrQueueTask>) {
        ocrQueueDatabase.useWriterConnection { transactor ->
            arcaeaOfflineDatabase.useWriterConnection { aoTransactor ->
                transactor.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                    aoTransactor.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                        val itemsFiltered =
                            items
                                .mapNotNull { task ->
                                    task.playResult?.let { task to it }
                                }.toMap()
                        val playResultsToSave = itemsFiltered.values.toTypedArray()
                        val tasksToDelete = itemsFiltered.keys.toList()

                        playResultRepository.upsertBatch(*playResultsToSave)
                        deleteBatch(tasksToDelete)
                    }
                }
            }
        }
    }
}
