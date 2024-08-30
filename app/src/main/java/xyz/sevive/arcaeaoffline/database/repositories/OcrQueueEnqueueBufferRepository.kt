package xyz.sevive.arcaeaoffline.database.repositories

import android.net.Uri
import android.util.Log
import kotlinx.coroutines.flow.Flow
import xyz.sevive.arcaeaoffline.database.daos.OcrQueueEnqueueBufferDao
import xyz.sevive.arcaeaoffline.database.entities.OcrQueueEnqueueBuffer


class OcrQueueEnqueueBufferRepository(private val dao: OcrQueueEnqueueBufferDao) {
    fun findUnchecked(): Flow<List<OcrQueueEnqueueBuffer>> = dao.findUnchecked()
    fun findChecked(): Flow<List<OcrQueueEnqueueBuffer>> = dao.findChecked()
    fun findShouldInsertUris(): Flow<List<Uri>> = dao.findShouldInsertUris()
    fun count(): Flow<Int> = dao.count()
    fun countChecked(): Flow<Int> = dao.countChecked()
    suspend fun insert(ocrQueueEnqueueBuffer: OcrQueueEnqueueBuffer): Long =
        dao.insert(ocrQueueEnqueueBuffer)

    suspend fun insertBatch(uris: List<Uri>): List<Long> {
        val wtf = dao.insertBatch(uris.map { OcrQueueEnqueueBuffer(uri = it) })
        Log.d("OCR", wtf.size.toString())
        return wtf
    }

    suspend fun update(ocrQueueEnqueueBuffer: OcrQueueEnqueueBuffer) =
        dao.update(ocrQueueEnqueueBuffer)

    suspend fun deleteChecked(): Int = dao.deleteChecked()
    suspend fun deleteAll(): Int = dao.deleteAll()
}
