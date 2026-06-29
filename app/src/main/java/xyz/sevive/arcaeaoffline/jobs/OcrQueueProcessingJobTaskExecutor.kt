package xyz.sevive.arcaeaoffline.jobs

import android.content.Context
import co.touchlab.kermit.Logger
import io.sentry.Sentry
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.firstOrNull
import xyz.sevive.arcaeaoffline.core.database.repositories.ChartInfoRepository
import xyz.sevive.arcaeaoffline.core.ocr.ImageHashesDatabase
import xyz.sevive.arcaeaoffline.core.ocr.device.DeviceOcrOnnxHelper
import xyz.sevive.arcaeaoffline.database.entities.OcrQueueTask
import xyz.sevive.arcaeaoffline.database.entities.OcrQueueTaskStatus
import xyz.sevive.arcaeaoffline.database.repositories.OcrQueueTaskRepository
import xyz.sevive.arcaeaoffline.helpers.ArcaeaPlayResultValidator
import xyz.sevive.arcaeaoffline.helpers.DeviceOcrHelper
import xyz.sevive.arcaeaoffline.helpers.OcrDependencyLoader
import kotlin.time.Clock

internal fun OcrQueueTask.copyWithException(exception: Exception) =
    this.copy(
        status = OcrQueueTaskStatus.ERROR,
        errorType = exception::class.qualifiedName,
        errorMessage = exception.message,
    )

interface OcrQueueProcessingJobTaskExecutor : AutoCloseable {
    suspend fun execute(task: OcrQueueTask)
}

class OcrQueueOcrImageTaskExecutor(
    private val context: Context,
    private val taskRepo: OcrQueueTaskRepository,
) : OcrQueueProcessingJobTaskExecutor {
    private val logger = Logger.withTag("OcrQueueProcessing.Image")

    private val ortSession =
        DeviceOcrOnnxHelper.createOrtSession(context)

    private val imageHashesSQLiteDatabase =
        OcrDependencyLoader.imageHashesSQLiteDatabase()

    private val imageHashesDatabase = ImageHashesDatabase(imageHashesSQLiteDatabase)

    private val kNearestModel = OcrDependencyLoader.kNearestModel()

    override fun close() {
        ortSession.close()
        imageHashesSQLiteDatabase.close()
    }

    override suspend fun execute(task: OcrQueueTask) {
        @Suppress("NAME_SHADOWING")
        var task = task.copy(status = OcrQueueTaskStatus.PROCESSING)
        taskRepo.update(task)
        logger.v { "Processing task ${task.id}" }

        try {
            val uri = task.fileUri

            val ocrResult =
                DeviceOcrHelper.ocrImage(
                    uri,
                    kNearestModel,
                    imageHashesDatabase,
                    ortSession = ortSession,
                )

            val playResult =
                DeviceOcrHelper.ocrResultToPlayResult(
                    uri,
                    context,
                    ocrResult,
                    fallbackDate = Clock.System.now(),
                )

            task =
                task.copy(
                    status = OcrQueueTaskStatus.DONE,
                    result = ocrResult,
                    playResult = playResult,
                    errorType = null,
                    errorMessage = null,
                )

            taskRepo.update(task)
        } catch (e: CancellationException) {
            // Rethrow and let [doWork] handle the rest
            throw e
        } catch (e: Exception) {
            task = task.copyWithException(e)
            logger.e(e) { "Error occurred at task ${task.id} ${task.fileUri}" }
            Sentry.captureException(e)
            taskRepo.update(task)
        }
    }
}

class OcrQueueFixTaskExecutor(
    private val chartInfoRepo: ChartInfoRepository,
    private val ocrQueueTaskRepo: OcrQueueTaskRepository,
) : OcrQueueProcessingJobTaskExecutor {
    override suspend fun execute(task: OcrQueueTask) {
        if (task.result == null || task.playResult == null) return

        // this is all possible songIds
        val hashResultLabels = task.result.songIdResults.map { it.label }
        val ratingClass = task.result.ratingClass

        for (songId in hashResultLabels) {
            val chartInfo = chartInfoRepo.find(songId, ratingClass).firstOrNull() ?: continue

            val newPlayResult = task.playResult.copy(songId = songId)
            if (ArcaeaPlayResultValidator.validate(newPlayResult, chartInfo).isEmpty()) {
                ocrQueueTaskRepo.update(task.copy(playResult = newPlayResult))
                return
            }
        }
    }

    override fun close() {}
}
