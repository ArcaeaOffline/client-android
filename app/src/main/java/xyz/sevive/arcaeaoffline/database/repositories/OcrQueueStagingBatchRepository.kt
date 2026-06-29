package xyz.sevive.arcaeaoffline.database.repositories

import xyz.sevive.arcaeaoffline.database.daos.OcrQueueStagingBatchDao
import xyz.sevive.arcaeaoffline.database.entities.OcrQueueStagingBatch
import xyz.sevive.arcaeaoffline.database.entities.OcrQueueStagingBatchWithItems

class OcrQueueStagingBatchRepository(
    private val dao: OcrQueueStagingBatchDao,
) {
    suspend fun getAllBatchWithItems(): List<OcrQueueStagingBatchWithItems> = dao.getAllBatchWithItems()

    suspend fun insert(item: OcrQueueStagingBatch): Long = dao.insert(item)

    suspend fun deleteByIds(ids: List<Long>) = dao.deleteByIds(ids)

    suspend fun getOrphans(): List<OcrQueueStagingBatch> = dao.getOrphans()
}
