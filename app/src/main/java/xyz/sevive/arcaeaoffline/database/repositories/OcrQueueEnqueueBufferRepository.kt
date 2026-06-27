package xyz.sevive.arcaeaoffline.database.repositories

import android.net.Uri
import kotlinx.coroutines.flow.Flow
import xyz.sevive.arcaeaoffline.database.daos.OcrQueueEnqueueBufferDao
import xyz.sevive.arcaeaoffline.database.entities.OcrQueueEnqueueBuffer
import xyz.sevive.arcaeaoffline.database.entities.OcrQueueUriType

class OcrQueueEnqueueBufferRepository(
    private val dao: OcrQueueEnqueueBufferDao,
) {
    suspend fun findByUriType(type: OcrQueueUriType): List<OcrQueueEnqueueBuffer> = dao.findByUriType(type)

    suspend fun findShouldInsertUris(): List<Uri> =
        dao
            .findShouldInsertBuffers()
            .filter { it.uriType == OcrQueueUriType.FILE }
            .map { it.uri }

    fun count(): Flow<Int> = dao.count()

    fun countChecked(): Flow<Int> = dao.countChecked()

    suspend fun insert(ocrQueueEnqueueBuffer: OcrQueueEnqueueBuffer): Long = dao.insert(ocrQueueEnqueueBuffer)

    suspend fun insertBatch(
        uris: Map<Uri, OcrQueueUriType>,
        batchId: Long,
    ): List<Long> =
        dao.insertBatch(
            uris.map { (uri, uriType) ->
                OcrQueueEnqueueBuffer(uri = uri, uriType = uriType, batchId = batchId)
            },
        )

    suspend fun update(item: OcrQueueEnqueueBuffer) = dao.update(item)

    suspend fun updateBatch(items: List<OcrQueueEnqueueBuffer>) = dao.updateBatch(items)

    suspend fun deleteById(id: Long) = dao.deleteById(id)

    suspend fun deleteChecked(): Int = dao.deleteChecked()

    suspend fun deleteAll(): Int = dao.deleteAll()
}
