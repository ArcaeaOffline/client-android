package xyz.sevive.arcaeaoffline.helpers

import android.app.Notification
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import xyz.sevive.arcaeaoffline.ArcaeaOfflineApplication
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.data.notification.Notifications
import xyz.sevive.arcaeaoffline.database.entities.OcrQueueEnqueueBuffer


class OcrQueueEnqueueCheckerJob(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {
    companion object {
        private const val LOG_TAG = "OcrQueueEnqCheckerJob"
        const val WORK_NAME = "OcrQueueEnqueueChecker"

        const val NOTIFICATION_CHANNEL = Notifications.CHANNEL_OCR_QUEUE_ENQUEUE_CHECKER_JOB
        const val NOTIFICATION_ID = Notifications.ID_OCR_QUEUE_ENQUEUE_CHECKER_JOB

        const val KEY_INPUT_CHECK_IS_IMAGE = "checkIsImage"
        const val KEY_INPUT_CHECK_IS_ARCAEA_IMAGE = "checkIsArcaeaImage"
    }

    private val notificationManager = NotificationManagerCompat.from(applicationContext)
    private val notificationTitle =
        applicationContext.getString(R.string.notif_title_ocr_queue_enqueue_checker_job)

    private data class WorkOptions(
        val checkIsImage: Boolean,
        val checkIsArcaeaImage: Boolean,
    )

    private fun getWorkOptions(): WorkOptions = WorkOptions(
        checkIsImage = inputData.getBoolean(KEY_INPUT_CHECK_IS_IMAGE, true),
        checkIsArcaeaImage = inputData.getBoolean(KEY_INPUT_CHECK_IS_ARCAEA_IMAGE, true)
    )

    private fun getRepo() =
        (applicationContext as ArcaeaOfflineApplication).ocrQueueDatabaseRepositoryContainer.ocrQueueEnqueueBufferRepo

    private fun getTaskRepo() =
        (applicationContext as ArcaeaOfflineApplication).ocrQueueDatabaseRepositoryContainer.ocrQueueTaskRepo

    private suspend fun cleanup() {
        val repo = getRepo()
        val taskRepo = getTaskRepo()

        val urisToEnqueue = repo.findShouldInsertUris().firstOrNull() ?: emptyList()
        taskRepo.insertBatch(urisToEnqueue, applicationContext)
        repo.deleteChecked()
    }

    private val queueScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val queue = SimpleChannelTaskQueue<OcrQueueEnqueueBuffer>(queueScope)

    private val progressListenScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var progressListenJob: Job? = null
    private val progress =
        combine(queue.isRunning, queue.progress, queue.progressTotal) { running, progress, total ->
            if (running && total > 0) progress to total
            else null
        }.stateIn(progressListenScope, SharingStarted.Eagerly, null)

    private fun createNotification(progress: Pair<Int, Int>?): Notification {
        val builder = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL)
            .setSmallIcon(R.drawable.ic_ocr).setContentTitle(notificationTitle)

        if (progress == null) {
            builder.setContentText(applicationContext.getString(R.string.general_please_wait))
            builder.setProgress(0, 0, true)
        } else {
            with(progress) {
                builder.setContentText("$first / $second")
                builder.setProgress(second, first, false)
            }
        }

        return builder.build()
    }

    private fun createForegroundInfo(): ForegroundInfo {
        val notification = createNotification(progress.value)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ForegroundInfo(
                NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            ForegroundInfo(NOTIFICATION_ID, notification)
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return createForegroundInfo()
    }

    override suspend fun doWork(): Result {
        try {
            setForeground(createForegroundInfo())

            cleanup()

            val repo = getRepo()

            val dbItems = repo.findUnchecked().firstOrNull() ?: return Result.failure()
            val workOptions = getWorkOptions()

            progressListenJob = progressListenScope.launch {
                progress.collect {
                    if (it == null || it.first % 5 == 0 || it.first == it.second) {
                        notificationManager.notify(NOTIFICATION_ID, createNotification(it))
                    }
                }
            }

            queue.start(dbItems) {
                var shouldInsert = true

                if (workOptions.checkIsImage) {
                    if (!OcrQueueHelper.isUriImage(it.uri, applicationContext)) {
                        shouldInsert = false
                    }
                }

                if (workOptions.checkIsArcaeaImage) {
                    if (!OcrQueueHelper.isUriArcaeaImage(it.uri, applicationContext)) {
                        shouldInsert = false
                    }
                }

                repo.update(
                    it.copy(checked = true, shouldInsert = shouldInsert)
                )
            }

            return Result.success()
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Unexpected error during enqueue checking", e)
            return Result.failure()
        } finally {
            cleanup()
            progressListenJob?.cancel()
            notificationManager.cancel(NOTIFICATION_ID)
        }
    }
}
