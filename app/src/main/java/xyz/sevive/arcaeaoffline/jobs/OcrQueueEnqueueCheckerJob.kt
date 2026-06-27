package xyz.sevive.arcaeaoffline.jobs

import android.Manifest
import android.app.Notification
import android.content.Context
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.room.immediateTransaction
import androidx.room.useWriterConnection
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import co.touchlab.kermit.Logger
import io.sentry.Sentry
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.Progress
import xyz.sevive.arcaeaoffline.data.notification.Notifications
import xyz.sevive.arcaeaoffline.database.OcrQueueDatabase
import xyz.sevive.arcaeaoffline.database.entities.OcrQueueBatchWithBuffers
import xyz.sevive.arcaeaoffline.database.entities.OcrQueueEnqueueBuffer
import xyz.sevive.arcaeaoffline.database.entities.OcrQueueEnqueueOptions
import xyz.sevive.arcaeaoffline.database.entities.OcrQueueUriType
import xyz.sevive.arcaeaoffline.database.repositories.OcrQueueEnqueueBatchRepository
import xyz.sevive.arcaeaoffline.database.repositories.OcrQueueEnqueueBufferRepository
import xyz.sevive.arcaeaoffline.database.repositories.OcrQueueTaskRepository
import xyz.sevive.arcaeaoffline.datastore.OcrQueuePreferences
import xyz.sevive.arcaeaoffline.datastore.OcrQueuePreferencesRepository
import xyz.sevive.arcaeaoffline.helpers.OcrQueueHelper
import xyz.sevive.arcaeaoffline.helpers.toWorkData
import kotlin.math.max
import kotlin.time.Duration.Companion.milliseconds

class OcrQueueEnqueueCheckerJob(
    context: Context,
    params: WorkerParameters,
    private val db: OcrQueueDatabase,
    private val bufferRepo: OcrQueueEnqueueBufferRepository,
    private val taskRepo: OcrQueueTaskRepository,
    private val batchRepo: OcrQueueEnqueueBatchRepository,
    private val preferencesRepo: OcrQueuePreferencesRepository,
) : CoroutineWorker(context, params) {
    companion object {
        private const val LOG_TAG = "OcrQueueEnqCheckerJob"
        const val WORK_NAME = "OcrQueueEnqueueChecker"

        const val NOTIFICATION_CHANNEL = Notifications.CHANNEL_OCR_QUEUE_ENQUEUE_CHECKER_JOB
        const val NOTIFICATION_ID = Notifications.ID_OCR_QUEUE_ENQUEUE_CHECKER_JOB
    }

    private val logger = Logger.withTag(LOG_TAG)

    private val notificationTitle =
        applicationContext.getString(R.string.notif_title_ocr_queue_enqueue_checker_job)
    private val notificationTextPleaseWait =
        applicationContext.getString(R.string.general_please_wait)
    private val notificationBuilder =
        NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL).apply {
            setSmallIcon(R.drawable.ic_ocr)
            setContentTitle(notificationTitle)
            setOnlyAlertOnce(true)
        }

    /**
     * Cleanup the database, which:
     *
     * * Convert all checked [OcrQueueEnqueueBuffer][xyz.sevive.arcaeaoffline.database.entities.OcrQueueEnqueueBuffer]s
     *   to [OcrQueueTask][xyz.sevive.arcaeaoffline.database.entities.OcrQueueTask]
     * * Removes all completed [OcrQueueEnqueueBatch][xyz.sevive.arcaeaoffline.database.entities.OcrQueueEnqueueBatch]s
     */
    private suspend fun cleanup() {
        logger.i { "Cleaning up" }

        db.useWriterConnection { transactor ->
            transactor.immediateTransaction {
                val urisToEnqueue = bufferRepo.findShouldInsertUris()
                if (urisToEnqueue.isNotEmpty()) {
                    taskRepo.insertBatch(urisToEnqueue)
                }
                bufferRepo.deleteChecked()
                batchRepo.deleteByIds(batchRepo.getOrphans().map { it.id })
            }
        }
    }

    private val progressFlow = MutableStateFlow(Progress.INDETERMINATE)

    private fun createNotification(progress: Progress): Notification {
        if (progress.isIndeterminate) {
            notificationBuilder.setContentText(notificationTextPleaseWait)
            notificationBuilder.setProgress(0, 0, true)
        } else {
            notificationBuilder.setContentText("${progress.current} / ${progress.total}")
            notificationBuilder.setProgress(progress.total, progress.current, false)
        }

        return notificationBuilder.build()
    }

    private fun createForegroundInfo(progress: Progress): ForegroundInfo {
        val notification = createNotification(progress)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ForegroundInfo(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
            )
        } else {
            ForegroundInfo(NOTIFICATION_ID, notification)
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo = createForegroundInfo(progressFlow.value)

    private suspend fun publishProgress(progress: Progress) {
        setForeground(createForegroundInfo(progress))
        setProgress(progress.toWorkData())
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun doWork(): Result {
        try {
            setForeground(getForegroundInfo())

            workload()

            return Result.success()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.e(e) { "Unexpected error during enqueue checking" }
            Sentry.captureException(e)
            return Result.failure()
        } finally {
            logger.i { "doWork cleaning up" }
            withContext(NonCancellable) {
                cleanup()
            }
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private suspend fun workload() =
        coroutineScope {
            // The progress listen job, handles both notification & work data
            // A manual throttling is applied
            @OptIn(FlowPreview::class)
            val progressListenJob =
                launch {
                    progressFlow
                        .sample(500.milliseconds)
                        .collect { progress -> publishProgress(progress) }
                }

            try {
                // first, if there's folder requests, process them.
                val folderBuffers = bufferRepo.findByUriType(OcrQueueUriType.FOLDER)
                scanFolder(folderBuffers)

                // folders done, reset progress
                progressFlow.update { Progress.INDETERMINATE }

                // now process the files we have
                val items = batchRepo.getAllBatchWithBuffers()
                if (items.isEmpty()) return@coroutineScope
                // total progress handled here
                progressFlow.update { Progress(total = items.sumOf { it.buffers.size }) }
                val preferences = preferencesRepo.preferencesFlow.firstOrNull() ?: OcrQueuePreferences()
                items.forEach { processBatch(it, preferences.parallelCount) }
            } catch (e: CancellationException) {
                throw e
            } finally {
                progressListenJob.cancel()
                // Make sure the final progress is published
                publishProgress(progressFlow.value)
            }
        }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private suspend fun scanFolder(buffers: List<OcrQueueEnqueueBuffer>) =
        withContext(Dispatchers.IO) {
            logger.i { "scanFolder preprocessing" }
            // set progress to intermediate
            // TODO: if any progress could be displayed?
            progressFlow.update { Progress.INDETERMINATE }
            buffers
                .forEach {
                    val fileUris = getFolderFileUris(it.uri)

                    db.useWriterConnection { transactor ->
                        transactor.immediateTransaction {
                            // A folder item should be removed when it's processed
                            bufferRepo.deleteById(it.id)
                            bufferRepo.insertBatch(fileUris.associateWith { OcrQueueUriType.FILE }, it.batchId)
                        }
                    }
                }
        }

    /**
     * Non-recursive. Only direct children are scanned.
     */
    private suspend fun getFolderFileUris(folderUri: Uri): List<Uri> =
        withContext(Dispatchers.IO) {
            logger.i { "Scanning folder: $folderUri" }

            val results = mutableListOf<Uri>()
            val context = applicationContext

            // For future CMP migrating: this...is only for Android.
            // For desktop, just use PlatformFile. It should be quite fast.
            try {
                val treeId = DocumentsContract.getTreeDocumentId(folderUri)
                val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(folderUri, treeId)

                val projection =
                    arrayOf(
                        DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                        DocumentsContract.Document.COLUMN_MIME_TYPE,
                    )

                context.contentResolver.query(childrenUri, projection, null, null, null)?.use { cursor ->
                    val idIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                    val mimeIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE)

                    while (cursor.moveToNext()) {
                        val docId = cursor.getString(idIndex)
                        val mimeType = cursor.getString(mimeIndex)
                        // Filter out non-file objects
                        if (mimeType == DocumentsContract.Document.MIME_TYPE_DIR) continue

                        val fileUri = DocumentsContract.buildDocumentUriUsingTree(folderUri, docId)
                        results.add(fileUri)
                    }
                }
            } catch (e: Exception) {
                logger.e(e) { "Failed to query ContentResolver for SAF" }
            }

            logger.i { "scanFolder got ${results.size} results in $folderUri" }
            results
        }

    private suspend fun processBatch(
        batchWithBuffers: OcrQueueBatchWithBuffers,
        parallelCount: Int,
    ) = coroutineScope {
        val options = batchWithBuffers.batch.options

        val inputChannel = Channel<OcrQueueEnqueueBuffer>(parallelCount * 2)
        val outputChannel = Channel<OcrQueueEnqueueBuffer>(capacity = 100)

        launch {
            batchWithBuffers.buffers.forEach { inputChannel.send(it) }
            inputChannel.close()
        }

        val dbWriterJob =
            launch {
                val chunk = mutableListOf<OcrQueueEnqueueBuffer>()

                for (result in outputChannel) {
                    chunk.add(result)
                    if (chunk.size >= 50) {
                        bufferRepo.updateBatch(chunk)
                        chunk.clear()
                    }
                }

                if (chunk.isNotEmpty()) {
                    bufferRepo.updateBatch(chunk)
                }
            }

        val workers =
            List(max(parallelCount, 1)) {
                launch {
                    for (item in inputChannel) {
                        val processed = processBuffer(item, options)
                        outputChannel.send(processed)
                        progressFlow.update { it.increment() }
                    }
                }
            }

        // Lifecycle
        workers.joinAll()
        outputChannel.close()
        dbWriterJob.join()
    }

    private suspend fun processBuffer(
        item: OcrQueueEnqueueBuffer,
        options: OcrQueueEnqueueOptions,
    ): OcrQueueEnqueueBuffer {
        if (item.checked) return item

        var shouldInsert = true
        try {
            if (options.checkIsImage && !OcrQueueHelper.isUriImage(item.uri)) {
                shouldInsert = false
            }
            if (options.checkIsArcaeaImage && !OcrQueueHelper.isUriArcaeaImage(item.uri)) {
                shouldInsert = false
            }
        } catch (e: Exception) {
            logger.e(e) { "Failed to check ${item.uri}" }
            shouldInsert = false
        }

        return item.copy(checked = true, shouldInsert = shouldInsert)
    }
}
