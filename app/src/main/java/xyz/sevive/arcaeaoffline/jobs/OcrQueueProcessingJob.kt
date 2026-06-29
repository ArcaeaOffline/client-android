package xyz.sevive.arcaeaoffline.jobs

import android.app.Notification
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.Progress
import xyz.sevive.arcaeaoffline.core.database.repositories.ChartInfoRepository
import xyz.sevive.arcaeaoffline.data.notification.Notifications
import xyz.sevive.arcaeaoffline.database.entities.OcrQueueTask
import xyz.sevive.arcaeaoffline.database.entities.OcrQueueTaskStatus
import xyz.sevive.arcaeaoffline.database.repositories.OcrQueueTaskRepository
import xyz.sevive.arcaeaoffline.helpers.ArcaeaPlayResultValidator
import xyz.sevive.arcaeaoffline.helpers.toWorkData
import kotlin.time.Duration.Companion.milliseconds

class OcrQueueProcessingJob(
    context: Context,
    params: WorkerParameters,
    private val taskRepo: OcrQueueTaskRepository,
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
            fun fromInt(value: Int) = entries.firstOrNull { it.value == value }
        }
    }

    private data class WorkOptions(
        val runMode: RunMode,
        val parallelCount: Int,
    )

    private fun parseRunMode(): RunMode {
        val runModeInput = inputData.getInt(DATA_RUN_MODE, 0)
        val result = RunMode.fromInt(runModeInput)
        if (result == null) logger.w { "Invalid RunMode $runModeInput, falling back to ${RunMode.NORMAL}" }
        return result ?: RunMode.NORMAL
    }

    private fun getWorkOptions(): WorkOptions =
        WorkOptions(
            runMode = parseRunMode(),
            parallelCount =
                inputData
                    .getInt(
                        DATA_PARALLEL_COUNT,
                        Runtime.getRuntime().availableProcessors() / 2,
                    ).coerceAtLeast(1),
        )

    private val notificationBuilder =
        NotificationCompat
            .Builder(applicationContext, NOTIFICATION_CHANNEL)
            .setSmallIcon(R.drawable.ic_ocr)
            .setContentTitle(applicationContext.getString(R.string.notif_title_ocr_queue_job))

    private val progressFlow = MutableStateFlow(Progress.INDETERMINATE)

    private fun createNotification(progress: Progress?): Notification {
        // legacy code says null value indicates job done
        if (progress == null) {
            notificationBuilder.setOngoing(false)
            notificationBuilder.setContentText(applicationContext.getString(R.string.general_action_done))
        } else {
            notificationBuilder.setOngoing(true)
            if (progress.isIndeterminate) {
                notificationBuilder.setContentText(applicationContext.getString(R.string.general_please_wait))
                notificationBuilder.setProgress(0, 0, true)
            } else {
                notificationBuilder.setContentText("${progress.current} / ${progress.total}")
                notificationBuilder.setProgress(progress.total, progress.current, false)
            }
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

    private suspend fun fetchTasks(runMode: RunMode): List<OcrQueueTask> =
        when (runMode) {
            RunMode.NORMAL -> {
                taskRepo
                    .findByStatus(
                        OcrQueueTaskStatus.IDLE,
                        OcrQueueTaskStatus.PROCESSING,
                        OcrQueueTaskStatus.ERROR,
                    ).firstOrNull()
                    .orEmpty()
            }

            RunMode.ALL -> {
                taskRepo.findAll().firstOrNull().orEmpty()
            }

            RunMode.SMART_FIX -> {
                taskRepo.findByStatus(OcrQueueTaskStatus.DONE).firstOrNull().orEmpty().filter { task ->
                    task.playResult?.let { playResult ->
                        val chartInfo = chartInfoRepo.find(playResult).firstOrNull() ?: return@let false
                        ArcaeaPlayResultValidator.validate(playResult, chartInfo).isNotEmpty()
                    } == true
                }
            }
        }

    private fun createTaskExecutor(runMode: RunMode): OcrQueueProcessingJobTaskExecutor =
        when (runMode) {
            RunMode.SMART_FIX -> {
                OcrQueueFixTaskExecutor(
                    chartInfoRepo,
                    taskRepo,
                )
            }

            RunMode.NORMAL,
            RunMode.ALL,
            -> {
                OcrQueueOcrImageTaskExecutor(
                    applicationContext,
                    taskRepo,
                )
            }
        }

    @androidx.annotation.RequiresPermission(android.Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun doWork(): Result {
        val workOptions = getWorkOptions()
        val tasks = fetchTasks(workOptions.runMode)

        if (tasks.isEmpty()) return Result.success()

        progressFlow.update { Progress(total = tasks.size) }
        setForeground(getForegroundInfo())

        try {
            // coroutineScope inherits the worker's cancellation context natively
            coroutineScope {
                @OptIn(FlowPreview::class)
                val progressPublishJob =
                    launch {
                        progressFlow
                            .sample(500.milliseconds)
                            .collect { publishProgress(it) }
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
                    progressPublishJob.cancel()
                }
            }

            return Result.success()
        } catch (e: CancellationException) {
            logger.i { "CancellationException caught, updating ongoing task status" }

            // Database cleaning up
            withContext(NonCancellable + Dispatchers.IO) {
                val processingTasks = taskRepo.findByStatus(OcrQueueTaskStatus.PROCESSING).firstOrNull()
                processingTasks?.forEach {
                    taskRepo.update(it.copyWithException(e))
                }
            }

            throw e
        } catch (e: Exception) {
            logger.e(e) { "Uncaught error during doWork()" }
            Sentry.configureScope {
                it.setContexts(WORK_NAME, workOptions)
                Sentry.captureException(e)
            }
            return Result.failure()
        } finally {
            // Reset progress for future runs if this worker instance is reused
            progressFlow.update { Progress.INDETERMINATE }
        }
    }

    private suspend fun processTasks(
        tasks: List<OcrQueueTask>,
        executor: OcrQueueProcessingJobTaskExecutor,
        parallelCount: Int,
    ) = coroutineScope {
        val channel = Channel<OcrQueueTask>(parallelCount)

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
                        progressFlow.update { it.increment() }
                    }
                }
            }
        }
    }
}
