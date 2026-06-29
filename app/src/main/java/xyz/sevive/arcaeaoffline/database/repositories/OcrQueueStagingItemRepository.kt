package xyz.sevive.arcaeaoffline.database.repositories

import android.net.Uri
import kotlinx.coroutines.flow.Flow
import xyz.sevive.arcaeaoffline.database.daos.OcrQueueStagingItemDao
import xyz.sevive.arcaeaoffline.database.entities.OcrQueueStagingItem
import xyz.sevive.arcaeaoffline.database.entities.OcrQueueStagingUriType

class OcrQueueStagingItemRepository(
    private val dao: OcrQueueStagingItemDao,
) {
    suspend fun findByUriType(type: OcrQueueStagingUriType): List<OcrQueueStagingItem> = dao.findByUriType(type)

    suspend fun findShouldInsertUris(): List<Uri> =
        dao
            .findShouldInsertItems()
            .filter { it.uriType == OcrQueueStagingUriType.FILE }
            .map { it.uri }

    fun count(): Flow<Int> = dao.count()

    fun countChecked(): Flow<Int> = dao.countChecked()

    suspend fun insert(ocrQueueStagingItem: OcrQueueStagingItem): Long = dao.insert(ocrQueueStagingItem)

    suspend fun insertBatch(
        uris: Map<Uri, OcrQueueStagingUriType>,
        batchId: Long,
    ): List<Long> =
        dao.insertBatch(
            uris.map { (uri, uriType) ->
                OcrQueueStagingItem(uri = uri, uriType = uriType, batchId = batchId)
            },
        )

    suspend fun update(item: OcrQueueStagingItem) = dao.update(item)

    suspend fun updateBatch(items: List<OcrQueueStagingItem>) = dao.updateBatch(items)

    suspend fun deleteById(id: Long) = dao.deleteById(id)

    suspend fun deleteChecked(): Int = dao.deleteChecked()

    suspend fun deleteAll(): Int = dao.deleteAll()
}
