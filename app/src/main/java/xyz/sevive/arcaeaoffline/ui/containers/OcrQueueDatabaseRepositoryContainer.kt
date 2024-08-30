package xyz.sevive.arcaeaoffline.ui.containers

import android.content.Context
import xyz.sevive.arcaeaoffline.database.OcrQueueDatabase
import xyz.sevive.arcaeaoffline.database.repositories.OcrQueueEnqueueBufferRepository
import xyz.sevive.arcaeaoffline.database.repositories.OcrQueueTaskRepositoryImpl

class OcrQueueDatabaseRepositoryContainer(private val context: Context) {
    private fun database() = OcrQueueDatabase.getDatabase(context)

    val ocrQueueTaskRepo by lazy {
        OcrQueueTaskRepositoryImpl(database().ocrQueueTaskDao())
    }

    val ocrQueueEnqueueBufferRepo by lazy {
        OcrQueueEnqueueBufferRepository(database().ocrQueueEnqueueBufferDao())
    }
}
