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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.data.notification.Notifications
import xyz.sevive.arcaeaoffline.database.entities.OcrQueueEnqueueBuffer
import xyz.sevive.arcaeaoffline.database.repositories.OcrQueueEnqueueBufferRepository
import xyz.sevive.arcaeaoffline.ui.containers.OcrQueueDatabaseRepositoryContainer
import kotlin.time.Duration.Companion.seconds


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

    private val repoContainer = OcrQueueDatabaseRepositoryContainer(applicationContext)
    private val repo = repoContainer.ocrQueueEnqueueBufferRepo
    private val taskRepo = repoContainer.ocrQueueTaskRepo

    private suspend fun cleanup() {
        Log.i(LOG_TAG, "Cleaning up")

        val urisToEnqueue = repo.findShouldInsertUris().firstOrNull() ?: emptyList()
        taskRepo.insertBatch(urisToEnqueue, applicationContext)
        repo.deleteChecked()
    }

    private val channelScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val channel = Channel<OcrQueueEnqueueBuffer>()

    private val _progress = MutableStateFlow(0)
    private val _progressTotal = MutableStateFlow(-1)

    private val progressLock = Mutex()
    private val progressListenScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var progressListenJob: Job? = null
    private val progress =
        combine(_progress, _progressTotal) { progress, total ->
            if (total == -1) null
            else progress to total
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

            val dbItems = repo.findUnchecked().firstOrNull() ?: return Result.failure()
            val workOptions = getWorkOptions()

            progressListenJob = progressListenScope.launch {
                progress.collect {
                    if (it == null || it.first % 5 == 0 || it.first == it.second) {
                        notificationManager.notify(NOTIFICATION_ID, createNotification(it))
                    }
                }
            }

            withContext(Dispatchers.IO) {
                _progressTotal.value = dbItems.size

                channelScope.launch {
                    dbItems.forEach { channel.send(it) }
                    channel.close()
                }.invokeOnCompletion {
                    Log.d(LOG_TAG, "Channel send complete")
                }

                delay(0.5.seconds)

                channel.consumeEach {
                    processItem(it, repo, workOptions)
                    progressLock.withLock { _progress.value += 1 }
                }
            }

            return Result.success()
        } catch (e: CancellationException) {
            Log.i(LOG_TAG, "CancellationException caught")
            withContext(NonCancellable) {
                cleanup()
                repo.deleteAll()
            }
            return Result.failure()
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Unexpected error during enqueue checking", e)
            withContext(NonCancellable) {
                cleanup()
            }
            return Result.failure()
        } finally {
            Log.i(LOG_TAG, "doWork finally cleaning up")
            channelScope.coroutineContext.cancelChildren()
            progressListenJob?.cancel()
            notificationManager.cancel(NOTIFICATION_ID)
        }
    }

    private suspend fun processItem(
        dbItem: OcrQueueEnqueueBuffer,
        repo: OcrQueueEnqueueBufferRepository,
        workOptions: WorkOptions,
    ) {
        var shouldInsert = true

        if (workOptions.checkIsImage) {
            if (!OcrQueueHelper.isUriImage(dbItem.uri, applicationContext)) {
                shouldInsert = false
            }
        }

        if (workOptions.checkIsArcaeaImage) {
            if (!OcrQueueHelper.isUriArcaeaImage(dbItem.uri, applicationContext)) {
                shouldInsert = false
            }
        }

        repo.update(
            dbItem.copy(checked = true, shouldInsert = shouldInsert)
        )
    }
}
