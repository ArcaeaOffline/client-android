package xyz.sevive.arcaeaoffline.jobs

import android.content.Context
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.driver.bundled.SQLITE_OPEN_CREATE
import androidx.sqlite.driver.bundled.SQLITE_OPEN_READWRITE
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.io.files.SystemFileSystem
import xyz.sevive.arcaeaoffline.core.ocr.ImageHashesDatabaseBuilder
import xyz.sevive.arcaeaoffline.data.OcrDependencyPaths
import xyz.sevive.arcaeaoffline.helpers.ArcaeaPackageHelper

class ImageHashesDatabaseBuilderJob(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    private val logger = Logger.withTag(LOG_TAG)

    companion object {
        const val NAME = "ImageHashesDatabaseBuilderJob"
        private const val LOG_TAG = "ImageHashesDbBuilderJob"
        const val KEY_PROGRESS = "progress"
        const val KEY_PROGRESS_TOTAL = "progressTotal"
    }

    override suspend fun doWork(): Result {
        val ocrDependencyPaths = OcrDependencyPaths()
        val path = ocrDependencyPaths.imageHashesDatabaseFile
        val arcaeaPackageHelper = ArcaeaPackageHelper(applicationContext)

        val collectScope = CoroutineScope(Dispatchers.Default)

        try {
            withContext(Dispatchers.IO) {
                if (SystemFileSystem.exists(path)) SystemFileSystem.delete(path)
                SystemFileSystem.createDirectories(path.parent ?: error("$path has no parent"))
            }

            BundledSQLiteDriver()
                .open(
                    path.toString(),
                    SQLITE_OPEN_READWRITE.or(SQLITE_OPEN_CREATE),
                ).use { conn ->
                    val builder = ImageHashesDatabaseBuilder(conn)

                    collectScope.launch {
                        builder.buildProgress.collectLatest {
                            setProgress(
                                workDataOf(KEY_PROGRESS to it?.first, KEY_PROGRESS_TOTAL to it?.second),
                            )
                        }
                    }

                    arcaeaPackageHelper.fillHashesDatabaseBuilderTasks(builder)
                    builder.build(hashSize = 16, highFreqFactor = 4)
                    arcaeaPackageHelper.buildHashesDatabaseCleanUp()
                }

            return Result.success()
        } catch (e: Exception) {
            logger.e(e) { "Error building image hashes database" }
            return Result.failure()
        }
    }
}
