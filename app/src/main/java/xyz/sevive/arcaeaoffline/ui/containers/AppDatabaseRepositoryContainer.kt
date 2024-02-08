package xyz.sevive.arcaeaoffline.ui.containers

import android.content.Context
import xyz.sevive.arcaeaoffline.database.AppDatabase
import xyz.sevive.arcaeaoffline.database.repositories.OcrHistoryRepository

class AppDatabaseRepositoryContainer(private val context: Context) {
    val ocrHistoryRepository by lazy {
        OcrHistoryRepository(AppDatabase.getDatabase(context).ocrHistoryDao())
    }
}
