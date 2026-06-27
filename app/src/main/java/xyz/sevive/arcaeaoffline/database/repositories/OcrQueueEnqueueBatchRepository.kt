package xyz.sevive.arcaeaoffline.database.repositories

import xyz.sevive.arcaeaoffline.database.daos.OcrQueueEnqueueBatchDao
import xyz.sevive.arcaeaoffline.database.entities.OcrQueueBatchWithBuffers
import xyz.sevive.arcaeaoffline.database.entities.OcrQueueEnqueueBatch

class OcrQueueEnqueueBatchRepository(
    private val dao: OcrQueueEnqueueBatchDao,
) {
    suspend fun getAllBatchWithBuffers(): List<OcrQueueBatchWithBuffers> = dao.getAllBatchWithBuffers()

    suspend fun insert(item: OcrQueueEnqueueBatch): Long = dao.insert(item)

    suspend fun deleteByIds(ids: List<Long>) = dao.deleteByIds(ids)

    suspend fun getOrphans(): List<OcrQueueEnqueueBatch> = dao.getOrphans()
}
