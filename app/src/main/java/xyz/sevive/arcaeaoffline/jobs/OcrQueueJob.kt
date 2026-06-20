package xyz.sevive.arcaeaoffline.jobs

import ai.onnxruntime.OrtSession
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.opencv.ml.KNearest
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.database.repositories.ChartInfoRepository
import xyz.sevive.arcaeaoffline.core.ocr.ImageHashesDatabase
import xyz.sevive.arcaeaoffline.core.ocr.device.DeviceOcrOnnxHelper
import xyz.sevive.arcaeaoffline.data.notification.Notifications
import xyz.sevive.arcaeaoffline.database.entities.OcrQueueTask
import xyz.sevive.arcaeaoffline.database.entities.OcrQueueTaskStatus
import xyz.sevive.arcaeaoffline.database.repositories.OcrQueueTaskRepository
import xyz.sevive.arcaeaoffline.helpers.ArcaeaPlayResultValidator
import xyz.sevive.arcaeaoffline.helpers.DeviceOcrHelper
import xyz.sevive.arcaeaoffline.helpers.OcrDependencyLoader
import kotlin.time.Clock

class OcrQueueJob(
    context: Context,
    params: WorkerParameters,
    private val ocrQueueTaskRepo: OcrQueueTaskRepository,
    private val chartInfoRepo: ChartInfoRepository,
) : CoroutineWorker(context, params) {
    companion object {
        private const val LOG_TAG = "OcrQueueJob"
        const val WORK_NAME = "OcrQueueJob"

        const val NOTIFICATION_CHANNEL = Notifications.CHANNEL_OCR_QUEUE_JOB
        const val NOTIFICATION_ID = Notifications.ID_OCR_QUEUE_JOB

        const val DATA_RUN_MODE = "run_mode"
        const val DATA_PARALLEL_COUNT = "parallel_count"
    }

    private val logger = Logger.withTag(LOG_TAG)

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

    private val workOptions = getWorkOptions()

    private val notificationManager = NotificationManagerCompat.from(applicationContext)

    private val channelScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val channel = Channel<OcrQueueTask>()

    private val progressCurrent = MutableStateFlow(0)
    private val progressTotal = MutableStateFlow(-1)
    private val progressLock = Mutex()
    private val progress =
        combine(progressCurrent, progressTotal) { p, t ->
            if (t > -1) p to t else null
        }.stateIn(channelScope, SharingStarted.WhileSubscribed(2500L), null)
    private var progressListenJob: Job? = null

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

    private fun createForegroundInfo(): ForegroundInfo {
        val notification = createNotification(progress.value)

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

    /**
     * * [RunMode.NORMAL]: Only process [OcrQueueTaskStatus.IDLE] and [OcrQueueTaskStatus.ERROR] tasks.
     * * [RunMode.ALL]: Process all tasks, no matter what their status is.
     * * [RunMode.SMART_FIX]: Try fixing those [OcrQueueTaskStatus.DONE] tasks with warnings.
     */
    enum class RunMode(
        val value: Int,
    ) {
        NORMAL(0),
        ALL(1),
        SMART_FIX(2),
        ;

        companion object {
            fun fromInt(value: Int) = entries.firstOrNull { it.value == value } ?: NORMAL
        }
    }

    @androidx.annotation.RequiresPermission(android.Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun doWork(): Result {
        setForeground(createForegroundInfo())

        val workOptions = getWorkOptions()

        progressListenJob =
            channelScope.launch {
                progress.collectLatest {
                    notificationManager.notify(NOTIFICATION_ID, createNotification(it))
                }
            }

        try {
            when (workOptions.runMode) {
                RunMode.NORMAL -> runNormal()
                RunMode.ALL -> runAll()
                RunMode.SMART_FIX -> runSmartFix()
            }

            return Result.success()
        } catch (e: CancellationException) {
            logger.i { "CancellationException caught" }
            channelScope.coroutineContext.cancelChildren()

            withContext(NonCancellable + Dispatchers.IO) {
                channelScope
                    .async {
                        ocrQueueTaskRepo.findByStatus(OcrQueueTaskStatus.PROCESSING).firstOrNull()?.forEach {
                            ocrQueueTaskRepo.update(it.copy(status = OcrQueueTaskStatus.ERROR, exception = e))
                        }
                    }.await()
            }

            return Result.failure()
        } catch (e: Throwable) {
            logger.e(e) { "Uncaught error during doWork()" }
            Sentry.configureScope {
                it.setContexts(WORK_NAME, workOptions)
                Sentry.captureException(e)
            }
            return Result.failure()
        } finally {
            progressListenJob?.cancel()
            notificationManager.cancel(NOTIFICATION_ID)
        }
    }

    private suspend fun processTask(
        scope: CoroutineScope,
        task: OcrQueueTask,
        taskRepo: OcrQueueTaskRepository,
        chartInfoRepo: ChartInfoRepository,
        ortSession: OrtSession,
        kNearestModel: KNearest,
        imageHashesDatabase: ImageHashesDatabase,
    ) {
        if (isStopped) return

        @Suppress("NAME_SHADOWING")
        var task = task.copy()
        task = task.copy(status = OcrQueueTaskStatus.PROCESSING)
        taskRepo.update(task)
        logger.v { "Processing task ${task.id}" }
        var shouldUpdateTask = true

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
                    applicationContext,
                    ocrResult,
                    fallbackDate = Clock.System.now(),
                )

            val warnings =
                scope
                    .async {
                        val chartInfo = chartInfoRepo.find(playResult).firstOrNull()
                        ArcaeaPlayResultValidator.validate(playResult, chartInfo)
                    }.await()

            task =
                task.copy(
                    status = OcrQueueTaskStatus.DONE,
                    result = ocrResult,
                    playResult = playResult,
                    warnings = warnings,
                    exception = null,
                )
        } catch (e: CancellationException) {
            logger.i { "Job (${task.id}) CancellationException caught" }
            shouldUpdateTask = false
        } catch (e: Exception) {
            task =
                task.copy(
                    status = OcrQueueTaskStatus.ERROR,
                    exception = e,
                )

            logger.e(e) { "Error occurred at task ${task.id} ${task.fileUri}" }
            Sentry.captureException(e)
        }

        if (shouldUpdateTask) taskRepo.update(task)
    }

    private suspend fun processTasks(tasks: List<OcrQueueTask>) {
        DeviceOcrOnnxHelper.createOrtSession(applicationContext).use { ortSession ->
            OcrDependencyLoader.imageHashesSQLiteDatabase().use { sqliteDb ->
                val kNearestModel = OcrDependencyLoader.kNearestModel()
                val imageHashesDatabase = ImageHashesDatabase(sqliteDb)

                withContext(Dispatchers.IO) {
                    launch {
                        progressTotal.value = tasks.size
                        tasks.forEach { channel.send(it) }
                        channel.close()
                    }

                    repeat(workOptions.parallelCount) {
                        launch {
                            channel.consumeEach {
                                processTask(
                                    scope = channelScope,
                                    task = it,
                                    taskRepo = ocrQueueTaskRepo,
                                    chartInfoRepo = chartInfoRepo,
                                    ortSession = ortSession,
                                    kNearestModel = kNearestModel,
                                    imageHashesDatabase = imageHashesDatabase,
                                )
                                progressLock.withLock { progressCurrent.value += 1 }
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun runNormal() {
        val tasks =
            ocrQueueTaskRepo
                .findByStatus(
                    OcrQueueTaskStatus.IDLE,
                    OcrQueueTaskStatus.PROCESSING,
                    OcrQueueTaskStatus.ERROR,
                ).firstOrNull() ?: return
        processTasks(tasks)
    }

    private suspend fun runAll() {
        val tasks = ocrQueueTaskRepo.findAll().firstOrNull() ?: return
        processTasks(tasks)
    }

    private suspend fun tryFixTask(task: OcrQueueTask) {
        if (task.result == null || task.playResult == null) return

        // this is all possible songIds
        val hashResultLabels = task.result.songIdResults.map { it.label }
        val ratingClass = task.result.ratingClass

        for (songId in hashResultLabels) {
            val chartInfo = chartInfoRepo.find(songId, ratingClass).firstOrNull() ?: continue

            val newPlayResult = task.playResult.copy(songId = songId)
            if (ArcaeaPlayResultValidator.validate(newPlayResult, chartInfo).isEmpty()) {
                ocrQueueTaskRepo.update(task.copy(playResult = newPlayResult, warnings = null))
                return
            }
        }
    }

    private suspend fun runSmartFix() {
        val taskWithWarnings = ocrQueueTaskRepo.findDoneWithWarning().firstOrNull() ?: return
        taskWithWarnings.forEach { tryFixTask(it) }
    }
}
