package xyz.sevive.arcaeaoffline.helpers

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.apache.commons.io.IOUtils
import org.opencv.core.Mat
import org.opencv.core.MatOfByte
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import org.threeten.bp.Instant
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.database.entities.Chart
import xyz.sevive.arcaeaoffline.core.database.entities.Score
import xyz.sevive.arcaeaoffline.core.ocr.device.DeviceOcrResult
import xyz.sevive.arcaeaoffline.core.ocr.device.ScreenshotDetect
import xyz.sevive.arcaeaoffline.ui.containers.ArcaeaOfflineDatabaseRepositoryContainerImpl
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

enum class OcrQueueTaskStatus { IDLE, PROCESSING, DONE, ERROR }

data class OcrQueueTask(
    val id: Int,
    var fileUri: Uri,
    var status: OcrQueueTaskStatus,
    var ocrResult: DeviceOcrResult? = null,
    var score: Score? = null,
    var chart: Chart? = null,
    var exception: Exception? = null,
)

class OcrQueueAddImageFilesManualCancellationException : CancellationException()

class OcrQueueManualCancellationException : CancellationException {
    constructor() : super("User cancelled the queue.\n* Press start button to retry this task.")

    constructor(context: Context) : super(
        context.getString(R.string.ocr_queue_user_cancel_exception_message)
    )
}

class OcrQueue {
    private val _ocrQueueTasksMap = mutableMapOf<Int, OcrQueueTask>()
    private val _ocrQueueTasks = MutableStateFlow(listOf<OcrQueueTask>())
    val ocrQueueTasks = _ocrQueueTasks.asStateFlow()
    private val queueStateFlowSyncLock = ReentrantLock()

    private val addImagesScope = CoroutineScope(Dispatchers.IO)
    private val ocrQueueScope = CoroutineScope(Dispatchers.Default)

    private val _queueRunning = MutableStateFlow(false)
    val queueRunning = _queueRunning.asStateFlow()

    private val _addImagesProgress = MutableStateFlow(-1)
    val addImagesProgress = _addImagesProgress.asStateFlow()
    private val _addImagesProgressTotal = MutableStateFlow(-1)
    val addImagesProgressTotal = _addImagesProgressTotal.asStateFlow()

    private var lastTaskId = 1
    private val taskIdAllocLock = ReentrantLock()

    private fun getNewTaskId(): Int {
        taskIdAllocLock.withLock {
            lastTaskId += 1
            return lastTaskId - 1
        }
    }

    private fun getNewTask(fileUri: Uri): OcrQueueTask {
        return OcrQueueTask(
            id = getNewTaskId(),
            fileUri = fileUri,
            status = OcrQueueTaskStatus.IDLE,
        )
    }

    private fun syncQueueStateFlow() {
        queueStateFlowSyncLock.withLock {
            _ocrQueueTasks.value = _ocrQueueTasksMap.values.toList()
        }
    }

    private fun guardNoModifyWhenQueueRunning(message: String? = null): Boolean {
        return if (queueRunning.value) {
            Log.w(LOG_TAG, message ?: "Cannot modify queue while running!")
            false
        } else true
    }

    private fun guardTaskInList(taskId: Int, message: String? = null): Boolean {
        val taskInList = _ocrQueueTasksMap.keys.find { it == taskId } != null
        if (!taskInList) Log.e(LOG_TAG, message ?: "Cannot find task $taskId in queue!")
        return taskInList
    }

    private fun guardTaskInList(task: OcrQueueTask, message: String? = null): Boolean {
        return guardTaskInList(task.id, message)
    }

    private fun guardTaskNotInList(taskId: Int, message: String? = null): Boolean {
        val taskInList = _ocrQueueTasksMap.keys.find { it == taskId } != null
        if (taskInList) Log.e(LOG_TAG, message ?: "Task $taskId already in queue!")
        return !taskInList
    }

    fun getTask(taskId: Int): OcrQueueTask? {
        return _ocrQueueTasksMap[taskId]
    }

    private fun addTask(task: OcrQueueTask) {
        if (!guardNoModifyWhenQueueRunning()) return
        if (!guardTaskNotInList(task.id)) return

        _ocrQueueTasksMap[task.id] = task
        syncQueueStateFlow()
    }

    private val modifyTaskLock = ReentrantLock()

    private fun modifyTask(task: OcrQueueTask) {
        if (!guardTaskInList(task)) return

        modifyTaskLock.withLock {
            _ocrQueueTasksMap[task.id] = task
        }
        syncQueueStateFlow()
    }

    fun modifyTaskScore(taskId: Int, score: Score) {
        if (!guardTaskInList(taskId)) return

        val task = _ocrQueueTasksMap[taskId]!!
        modifyTask(task.copy(score = score))
    }

    fun deleteTask(taskId: Int) {
        if (!guardNoModifyWhenQueueRunning()) return
        if (!guardTaskInList(taskId)) return

        _ocrQueueTasksMap.remove(taskId)
        syncQueueStateFlow()
    }

    fun clear() {
        _ocrQueueTasksMap.clear()
        syncQueueStateFlow()
    }

    private fun isImage(byteArray: ByteArray): Boolean {
        // Know if a file is a image in Java/Android
        // https://stackoverflow.com/a/18499840/16484891
        // CC BY-SA 3.0
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size, options)
        return options.outWidth != -1 && options.outHeight != -1
    }

    private fun isImage(imageUri: Uri, context: Context): Boolean {
        val inputStream = context.contentResolver.openInputStream(imageUri) ?: return false
        val byteArray = inputStream.use { IOUtils.toByteArray(inputStream) }
        return isImage(byteArray)
    }

    private fun getTaskFromImageFile(
        imageUri: Uri,
        context: Context,
        checkIsImage: Boolean = true,
        detectScreenshot: Boolean = true,
    ): OcrQueueTask? {
        if (checkIsImage && !isImage(imageUri, context)) return null

        if (detectScreenshot) {
            val inputStream = context.contentResolver.openInputStream(imageUri) ?: return null
            val byteArray = inputStream.use { IOUtils.toByteArray(inputStream) }

            val img = Imgcodecs.imdecode(MatOfByte(*byteArray), Imgcodecs.IMREAD_COLOR)
            val imgHsv = Mat()
            Imgproc.cvtColor(img, imgHsv, Imgproc.COLOR_BGR2HSV)
            if (!ScreenshotDetect.isArcaeaScreenshot(imgHsv)) return null
        }

        return getNewTask(imageUri)
    }

    suspend fun addImageFiles(
        uris: List<Uri>,
        context: Context,
        checkIsImage: Boolean = true,
        detectScreenshot: Boolean = true,
    ) {
        _addImagesProgress.value = 0
        _addImagesProgressTotal.value = uris.size

        val jobHandle = addImagesScope.launch {
            val tasks = uris.map {
                async {
                    try {
                        return@async getTaskFromImageFile(
                            it,
                            context,
                            checkIsImage = checkIsImage,
                            detectScreenshot = detectScreenshot,
                        )
                    } catch (e: Exception) {
                        Log.e(LOG_TAG, "Error adding uri $it", e)
                        return@async null
                    } finally {
                        _addImagesProgress.value += 1
                    }
                }
            }.awaitAll()

            tasks.filterNotNull().forEach { addTask(it) }
        }

        jobHandle.join()

        _addImagesProgress.value = -1
        _addImagesProgressTotal.value = -1
    }

    fun stopAddImageFiles() {
        if (_addImagesProgressTotal.value < 0) {
            Log.w(LOG_TAG, "There's no active add image progress currently!")
            return
        }

        addImagesScope.coroutineContext.cancelChildren(
            OcrQueueAddImageFilesManualCancellationException()
        )
    }

    private suspend fun processTask(task: OcrQueueTask, context: Context) {
        if (task.status == OcrQueueTaskStatus.DONE) return

        var taskCopy = task.copy(status = OcrQueueTaskStatus.PROCESSING)
        Log.d(LOG_TAG, "Processing task ${taskCopy.id}")
        modifyTask(task)

        try {
            val ocrResult = DeviceOcrHelper.ocrImage(task.fileUri, context)
            val score = DeviceOcrHelper.ocrResultToScore(
                task.fileUri,
                context,
                ocrResult,
                fallbackDate = Instant.now(),
            )

            val arcaeaOfflineDatabaseRepositoryContainer =
                ArcaeaOfflineDatabaseRepositoryContainerImpl(context)
            val chart = ChartFactory.getChart(
                arcaeaOfflineDatabaseRepositoryContainer, ocrResult.songId, ocrResult.ratingClass
            )

            taskCopy = taskCopy.copy(
                status = OcrQueueTaskStatus.DONE,
                ocrResult = ocrResult,
                score = score,
                chart = chart,
            )
        } catch (e: Exception) {
            taskCopy = taskCopy.copy(
                status = OcrQueueTaskStatus.ERROR,
                exception = e,
            )
            if (e !is OcrQueueManualCancellationException) {
                Log.e(LOG_TAG, "Error occurred at task ${task.id} ${task.fileUri}", e)
            }
        }

        modifyTask(taskCopy)
    }

    fun tryStopQueue(context: Context? = null) {
        if (!_queueRunning.value) {
            Log.w(LOG_TAG, "Queue hasn't started!")
            return
        }

        ocrQueueScope.coroutineContext.cancelChildren(
            if (context != null) {
                OcrQueueManualCancellationException(context)
            } else {
                OcrQueueManualCancellationException()
            }
        )
    }

    suspend fun startQueue(context: Context) {
        if (_queueRunning.value) {
            Log.w(LOG_TAG, "Queue has already started!")
            return
        }

        _queueRunning.value = true

        val jobHandle = ocrQueueScope.launch {
            _ocrQueueTasksMap.values.map {
                val task = it.copy()
                async { processTask(task, context) }
            }.awaitAll()
        }

        jobHandle.join()
        _queueRunning.value = false
    }


    companion object {
        const val LOG_TAG = "OcrQueueCore"
    }
}
