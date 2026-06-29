package xyz.sevive.arcaeaoffline.jobs

import android.app.Notification
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import co.touchlab.kermit.Logger
import io.sentry.Sentry
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.database.repositories.ChartInfoRepository
import xyz.sevive.arcaeaoffline.data.notification.Notifications
import xyz.sevive.arcaeaoffline.database.entities.OcrQueueTask
import xyz.sevive.arcaeaoffline.database.entities.OcrQueueTaskStatus
import xyz.sevive.arcaeaoffline.database.repositories.OcrQueueTaskRepository
import xyz.sevive.arcaeaoffline.helpers.ArcaeaPlayResultValidator

class OcrQueueProcessingJob(
    context: Context,
    params: WorkerParameters,
    private val ocrQueueTaskRepo: OcrQueueTaskRepository,
    private val chartInfoRepo: ChartInfoRepository,
) : CoroutineWorker(context, params) {
    companion object {
        private const val LOG_TAG = "OcrQueueProcessingJob"
        const val WORK_NAME = "OcrQueueProcessingJob"

        const val NOTIFICATION_CHANNEL = Notifications.CHANNEL_OCR_QUEUE_JOB
        const val NOTIFICATION_ID = Notifications.ID_OCR_QUEUE_JOB

        const val DATA_RUN_MODE = "run_mode"
        const val DATA_PARALLEL_COUNT = "parallel_count"
    }

    private val logger = Logger.withTag(LOG_TAG)

    enum class RunMode(
        val value: Int,
    ) {
        /** Only process [OcrQueueTaskStatus.IDLE] and [OcrQueueTaskStatus.ERROR] tasks. */
        NORMAL(0),

        /** Process all tasks, no matter what their status is. */
        ALL(1),

        /** Try fixing those [OcrQueueTaskStatus.DONE] tasks with warnings. */
        SMART_FIX(2),
        ;

        companion object {
            fun fromInt(value: Int) = entries.firstOrNull { it.value == value } ?: NORMAL
        }
    }

    private data class WorkOptions(
        val runMode: RunMode,
        val parallelCount: Int,
    )

    private fun getWorkOptions(): WorkOptions =
        WorkOptions(
            runMode = RunMode.fromInt(inputData.getInt(DATA_RUN_MODE, RunMode.NORMAL.value)),
            parallelCount =
                inputData.getInt(
                    DATA_PARALLEL_COUNT,
                    Runtime.getRuntime().availableProcessors() / 2,
                ),
        )

    private val notificationManager = NotificationManagerCompat.from(applicationContext)

    private val progressCurrent = MutableStateFlow(0)
    private val progressTotal = MutableStateFlow(-1)
    private val progressLock = Mutex()
    private val progress =
        combine(progressCurrent, progressTotal) { p, t ->
            if (t > -1) p to t else null
        }

    private fun createNotification(progress: Pair<Int, Int>?): Notification {
        val builder =
            NotificationCompat
                .Builder(applicationContext, NOTIFICATION_CHANNEL)
                .setSmallIcon(R.drawable.ic_ocr)
                .setContentTitle(applicationContext.getString(R.string.notif_title_ocr_queue_job))

        val progressDone = progress == null
        if (progressDone) {
            builder.setOngoing(false)
            builder.setContentText(applicationContext.getString(R.string.general_action_done))
        } else {
            builder.setOngoing(true)
            progress.let {
                builder.setContentText("${it.first} / ${it.second}")
                if (it.second > -1) {
                    builder.setProgress(it.second, it.first, false)
                } else {
                    builder.setProgress(0, 0, true)
                }
            }
        }

        return builder.build()
    }

    private suspend fun createForegroundInfo(): ForegroundInfo {
        val notification = createNotification(progress.firstOrNull() ?: (0 to -1))

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

    override suspend fun getForegroundInfo(): ForegroundInfo = createForegroundInfo()

    private suspend fun fetchTasks(runMode: RunMode): List<OcrQueueTask> =
        when (runMode) {
            RunMode.NORMAL -> {
                ocrQueueTaskRepo
                    .findByStatus(
                        OcrQueueTaskStatus.IDLE,
                        OcrQueueTaskStatus.PROCESSING,
                        OcrQueueTaskStatus.ERROR,
                    ).firstOrNull()
                    .orEmpty()
            }

            RunMode.ALL -> {
                ocrQueueTaskRepo.findAll().firstOrNull().orEmpty()
            }

            RunMode.SMART_FIX -> {
                ocrQueueTaskRepo.findByStatus(OcrQueueTaskStatus.DONE).firstOrNull().orEmpty().filter { task ->
                    task.playResult?.let { playResult ->
                        val chartInfo = chartInfoRepo.find(playResult).firstOrNull() ?: return@let false
                        ArcaeaPlayResultValidator.validate(playResult, chartInfo).isNotEmpty()
                    } == true
                }
            }
        }

    private fun createTaskExecutor(runMode: RunMode): OcrQueueJobTaskExecutor =
        when (runMode) {
            RunMode.SMART_FIX -> {
                OcrQueueFixTaskExecutor(
                    chartInfoRepo,
                    ocrQueueTaskRepo,
                )
            }

            RunMode.NORMAL,
            RunMode.ALL,
            -> {
                OcrQueueOcrImageTaskExecutor(
                    applicationContext,
                    ocrQueueTaskRepo,
                )
            }
        }

    @androidx.annotation.RequiresPermission(android.Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun doWork(): Result {
        val workOptions = getWorkOptions()
        val tasks = fetchTasks(workOptions.runMode)

        if (tasks.isEmpty()) return Result.success()

        progressTotal.value = tasks.size
        setForeground(getForegroundInfo())

        try {
            // coroutineScope inherits the worker's cancellation context natively
            coroutineScope {
                val progressJob =
                    launch {
                        progress.collectLatest {
                            notificationManager.notify(NOTIFICATION_ID, createNotification(it))
                        }
                    }

                try {
                    createTaskExecutor(workOptions.runMode).use { executor ->
                        processTasks(
                            tasks,
                            executor,
                            workOptions.parallelCount,
                        )
                    }
                } finally {
                    progressJob.cancel()
                }
            }

            return Result.success()
        } catch (e: CancellationException) {
            logger.i { "CancellationException caught" }

            // Database cleaning up
            withContext(NonCancellable + Dispatchers.IO) {
                val processingTasks = ocrQueueTaskRepo.findByStatus(OcrQueueTaskStatus.PROCESSING).firstOrNull()
                processingTasks?.forEach {
                    ocrQueueTaskRepo.update(it.copyWithException(e))
                }
            }

            throw e
        } catch (e: Throwable) {
            logger.e(e) { "Uncaught error during doWork()" }
            Sentry.configureScope {
                it.setContexts(WORK_NAME, workOptions)
                Sentry.captureException(e)
            }
            return Result.failure()
        } finally {
            notificationManager.cancel(NOTIFICATION_ID)

            // Reset progress for future runs if this worker instance is reused
            progressCurrent.value = 0
            progressTotal.value = -1
        }
    }

    private suspend fun processTasks(
        tasks: List<OcrQueueTask>,
        executor: OcrQueueJobTaskExecutor,
        parallelCount: Int,
    ) = coroutineScope {
        val channel = Channel<OcrQueueTask>(Channel.UNLIMITED)

        launch {
            tasks.forEach { channel.send(it) }
            channel.close()
        }

        repeat(parallelCount) {
            launch(Dispatchers.IO) {
                for (task in channel) {
                    try {
                        executor.execute(task)
                    } finally {
                        progressLock.withLock {
                            progressCurrent.value += 1
                        }
                    }
                }
            }
        }
    }
}
