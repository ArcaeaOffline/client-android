package xyz.sevive.arcaeaoffline.jobs

import android.content.Context
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.driver.bundled.SQLITE_OPEN_CREATE
import androidx.sqlite.driver.bundled.SQLITE_OPEN_READWRITE
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import co.touchlab.kermit.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.io.files.SystemFileSystem
import xyz.sevive.arcaeaoffline.core.Progress
import xyz.sevive.arcaeaoffline.core.ocr.ImageHashesDatabaseBuilder
import xyz.sevive.arcaeaoffline.data.OcrDependencyPaths
import xyz.sevive.arcaeaoffline.helpers.ArcaeaPackageHelper
import xyz.sevive.arcaeaoffline.helpers.toWorkData
import kotlin.time.Duration.Companion.milliseconds

class ImageHashesDatabaseBuilderJob(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    private val logger = Logger.withTag(LOG_TAG)

    companion object {
        const val NAME = "ImageHashesDatabaseBuilderJob"
        private const val LOG_TAG = "ImageHashesDbBuilderJob"
    }

    private val progressFlow = MutableStateFlow(Progress.INDETERMINATE)

    override suspend fun doWork(): Result {
        val ocrDependencyPaths = OcrDependencyPaths()
        val path = ocrDependencyPaths.imageHashesDatabaseFile
        val arcaeaPackageHelper = ArcaeaPackageHelper(applicationContext)

        try {
            return coroutineScope {
                @OptIn(FlowPreview::class)
                val progressPublishJob =
                    launch {
                        progressFlow
                            .sample(100.milliseconds)
                            .collect { setProgress(it.toWorkData()) }
                    }

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

                        val progressUpdateJob =
                            launch {
                                builder.buildProgress.collect { progressFlow.value = it ?: Progress.INDETERMINATE }
                            }

                        arcaeaPackageHelper.fillHashesDatabaseBuilderTasks(builder)
                        builder.build(hashSize = 16, highFreqFactor = 4)
                        arcaeaPackageHelper.buildHashesDatabaseCleanUp()

                        progressUpdateJob.cancelAndJoin()
                    }

                progressPublishJob.cancelAndJoin()
                Result.success()
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e

            logger.e(e) { "Error building image hashes database" }
            return Result.failure()
        }
    }
}
