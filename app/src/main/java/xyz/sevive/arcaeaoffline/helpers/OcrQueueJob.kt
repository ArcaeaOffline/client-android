package xyz.sevive.arcaeaoffline.helpers

import ai.onnxruntime.OrtSession
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
import org.threeten.bp.Instant
import xyz.sevive.arcaeaoffline.ArcaeaOfflineApplication
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.database.repositories.ChartInfoRepository
import xyz.sevive.arcaeaoffline.core.ocr.ImageHashesDatabase
import xyz.sevive.arcaeaoffline.core.ocr.OcrDependencyLoader
import xyz.sevive.arcaeaoffline.core.ocr.device.DeviceOcrOnnxHelper
import xyz.sevive.arcaeaoffline.data.notification.Notifications
import xyz.sevive.arcaeaoffline.database.entities.OcrQueueTask
import xyz.sevive.arcaeaoffline.database.entities.OcrQueueTaskStatus
import xyz.sevive.arcaeaoffline.database.repositories.OcrQueueTaskRepository


class OcrQueueJob(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    companion object {
        private const val LOG_TAG = "OcrQueueJob"
        const val WORK_NAME = "OcrQueueJob"

        const val NOTIFICATION_CHANNEL = Notifications.CHANNEL_OCR_QUEUE_JOB
        const val NOTIFICATION_ID = Notifications.ID_OCR_QUEUE_JOB

        const val DATA_RUN_MODE = "run_mode"
        const val DATA_PARALLEL_COUNT = "parallel_count"
    }

    private data class WorkOptions(
        val runMode: RunMode,
        val parallelCount: Int,
    )

    private fun getWorkOptions(): WorkOptions {
        return WorkOptions(
            runMode = RunMode.fromInt(inputData.getInt(DATA_RUN_MODE, RunMode.NORMAL.value)),
            parallelCount = inputData.getInt(
                DATA_PARALLEL_COUNT, Runtime.getRuntime().availableProcessors() / 2
            ),
        )
    }

    private val workOptions = getWorkOptions()

    private val notificationManager = NotificationManagerCompat.from(applicationContext)

    private val channelScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val channel = Channel<OcrQueueTask>()

    private val _progress = MutableStateFlow(0)
    private val _progressTotal = MutableStateFlow(-1)
    private val progressLock = Mutex()
    private val progress = combine(_progress, _progressTotal) { p, t ->
        if (t > -1) p to t else null
    }.stateIn(channelScope, SharingStarted.WhileSubscribed(2500L), null)
    private var progressListenJob: Job? = null

    private fun createNotification(progress: Pair<Int, Int>?): Notification {
        val builder = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL)
            .setSmallIcon(R.drawable.ic_ocr)
            .setContentTitle(applicationContext.getString(R.string.notif_title_ocr_queue_job))

        val progressDone = progress == null
        if (progressDone) {
            builder.setOngoing(false)
            builder.setContentText(applicationContext.getString(R.string.general_action_done))
        } else {
            builder.setOngoing(true)
            progress?.let {
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
                NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            ForegroundInfo(NOTIFICATION_ID, notification)
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return createForegroundInfo()
    }

    /**
     * * [RunMode.NORMAL]: Only process [OcrQueueTaskStatus.IDLE] and [OcrQueueTaskStatus.ERROR] tasks.
     * * [RunMode.ALL]: Process all tasks, no matter what their status is.
     * * [RunMode.SMART_FIX]: Try fixing those [OcrQueueTaskStatus.DONE] tasks with warnings.
     */
    enum class RunMode(val value: Int) {
        NORMAL(0), ALL(1), SMART_FIX(2);

        companion object {
            fun fromInt(value: Int) = entries.firstOrNull { it.value == value } ?: NORMAL
        }
    }

    private val repo =
        (applicationContext as ArcaeaOfflineApplication).ocrQueueDatabaseRepositoryContainer.ocrQueueTaskRepo

    private val chartInfoRepo =
        (applicationContext as ArcaeaOfflineApplication).arcaeaOfflineDatabaseRepositoryContainer.chartInfoRepo

    override suspend fun doWork(): Result {
        setForeground(createForegroundInfo())

        val workOptions = getWorkOptions()

        progressListenJob = channelScope.launch {
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
            Log.i(LOG_TAG, "CancellationException caught")
            channelScope.coroutineContext.cancelChildren()

            withContext(NonCancellable + Dispatchers.IO) {
                channelScope.async {
                    repo.findByStatus(OcrQueueTaskStatus.PROCESSING).firstOrNull()?.forEach {
                        repo.update(it.copy(status = OcrQueueTaskStatus.ERROR, exception = e))
                    }
                }.await()
            }

            return Result.failure()
        } catch (e: Throwable) {
            Log.e(LOG_TAG, "Uncaught error during doWork()", e)
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

        @Suppress("NAME_SHADOWING") var task = task.copy()
        task = task.copy(status = OcrQueueTaskStatus.PROCESSING)
        taskRepo.update(task)
        Log.v(LOG_TAG, "Processing task ${task.id}")
        var shouldUpdateTask = true

        try {
            val uri = task.fileUri

            val ocrResult = scope.async {
                DeviceOcrHelper.ocrImage(
                    uri,
                    applicationContext,
                    kNearestModel,
                    imageHashesDatabase,
                    ortSession = ortSession
                )
            }.await()

            val playResult = scope.async {
                DeviceOcrHelper.ocrResultToPlayResult(
                    uri, applicationContext, ocrResult, fallbackDate = Instant.now()
                )
            }.await()

            val warnings = scope.async {
                val chartInfo = chartInfoRepo.find(playResult).firstOrNull()
                ArcaeaPlayResultValidator.validateScore(playResult, chartInfo)
            }.await()

            task = task.copy(
                status = OcrQueueTaskStatus.DONE,
                result = ocrResult,
                playResult = playResult,
                warnings = warnings,
                exception = null,
            )
        } catch (e: CancellationException) {
            Log.i(LOG_TAG, "Job (${task.id}) CancellationException caught")
            shouldUpdateTask = false
        } catch (e: Exception) {
            task = task.copy(
                status = OcrQueueTaskStatus.ERROR,
                exception = e,
            )

            Log.e(LOG_TAG, "Error occurred at task ${task.id} ${task.fileUri}", e)
            Sentry.captureException(e)
        }

        if (shouldUpdateTask) taskRepo.update(task)
    }

    private suspend fun processTasks(tasks: List<OcrQueueTask>) {
        DeviceOcrOnnxHelper.createOrtSession(applicationContext).use { ortSession ->
            OcrDependencyLoader.imageHashesSQLiteDatabase(applicationContext).use { sqliteDb ->
                val kNearestModel = OcrDependencyLoader.kNearestModel(applicationContext)
                val imageHashesDatabase = ImageHashesDatabase(sqliteDb)

                withContext(Dispatchers.IO) {
                    launch {
                        _progressTotal.value = tasks.size
                        tasks.forEach { channel.send(it) }
                        channel.close()
                    }

                    repeat(workOptions.parallelCount) {
                        launch {
                            channel.consumeEach {
                                processTask(
                                    scope = channelScope,
                                    task = it,
                                    taskRepo = repo,
                                    chartInfoRepo = chartInfoRepo,
                                    ortSession = ortSession,
                                    kNearestModel = kNearestModel,
                                    imageHashesDatabase = imageHashesDatabase,
                                )
                                progressLock.withLock { _progress.value += 1 }
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun runNormal() {
        val tasks = repo.findByStatus(
            OcrQueueTaskStatus.IDLE, OcrQueueTaskStatus.PROCESSING, OcrQueueTaskStatus.ERROR
        ).firstOrNull() ?: return
        processTasks(tasks)
    }

    private suspend fun runAll() {
        val tasks = repo.findAll().firstOrNull() ?: return
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
            if (ArcaeaPlayResultValidator.validateScore(newPlayResult, chartInfo).isEmpty()) {
                repo.update(task.copy(playResult = newPlayResult, warnings = null))
                return
            }
        }
    }

    private suspend fun runSmartFix() {
        val taskWithWarnings = repo.findDoneWithWarning().firstOrNull() ?: return
        taskWithWarnings.forEach { tryFixTask(it) }
    }
}
