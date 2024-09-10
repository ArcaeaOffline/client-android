package xyz.sevive.arcaeaoffline.core.ocr

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import io.requery.android.database.sqlite.SQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import xyz.sevive.arcaeaoffline.data.OcrDependencyPaths
import xyz.sevive.arcaeaoffline.helpers.ArcaeaPackageHelper


class ImageHashesDatabaseBuilderJob(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {
    companion object {
        const val NAME = "ImageHashesDatabaseBuilderJob"
        private const val LOG_TAG = "ImageHashesDbBuilderJob"
        const val KEY_PROGRESS = "progress"
        const val KEY_PROGRESS_TOTAL = "progressTotal"
    }

    override suspend fun doWork(): Result {
        val ocrDependencyPaths = OcrDependencyPaths(applicationContext)
        val file = ocrDependencyPaths.imageHashesDatabaseFile
        val arcaeaPackageHelper = ArcaeaPackageHelper(applicationContext)

        val collectScope = CoroutineScope(Dispatchers.Default)

        try {
            if (file.exists()) file.delete()

            SQLiteDatabase.openOrCreateDatabase(file, null).use { sqliteDb ->
                val builder = ImageHashesDatabaseBuilder(sqliteDb)

                collectScope.launch {
                    builder.buildProgress.collectLatest {
                        setProgress(
                            workDataOf(KEY_PROGRESS to it?.first, KEY_PROGRESS_TOTAL to it?.second)
                        )
                    }
                }

                arcaeaPackageHelper.fillHashesDatabaseBuilderTasks(builder)
                builder.build(hashSize = 16, highFreqFactor = 4)
                arcaeaPackageHelper.buildHashesDatabaseCleanUp()
            }

            return Result.success()
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Error building image hashes database", e)
            return Result.failure()
        }
    }
}
