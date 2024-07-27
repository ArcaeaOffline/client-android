package xyz.sevive.arcaeaoffline.helpers

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.apache.commons.io.IOUtils
import org.opencv.core.Mat
import org.opencv.core.MatOfByte
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import org.threeten.bp.Instant
import xyz.sevive.arcaeaoffline.core.database.entities.Chart
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.core.ocr.device.DeviceOcrResult
import xyz.sevive.arcaeaoffline.core.ocr.device.ScreenshotDetect
import xyz.sevive.arcaeaoffline.ui.containers.ArcaeaOfflineDatabaseRepositoryContainerImpl
import java.util.concurrent.ConcurrentHashMap

enum class OcrQueueTaskStatus { IDLE, PROCESSING, DONE, ERROR }

data class OcrQueueTask(
    val id: Int,
    var fileUri: Uri,
    var status: OcrQueueTaskStatus,
    var ocrResult: DeviceOcrResult? = null,
    var playResult: PlayResult? = null,
    var chart: Chart? = null,
    var exception: Exception? = null,
)

class OcrQueue {
    private val _parallelCount = MutableStateFlow(Runtime.getRuntime().availableProcessors())
    val parallelCount = _parallelCount.asStateFlow()

    val ocrQueueTasksMap = ConcurrentHashMap<Int, OcrQueueTask>()
    private val _taskUpdatedFlow = MutableSharedFlow<Int>()
    val taskUpdatedFlow = _taskUpdatedFlow.asSharedFlow()

    private val addImagesScope = CoroutineScope(Dispatchers.IO)
    private val ocrQueueScope = CoroutineScope(Dispatchers.Default)

    private val addImagesChannelQueue = SimpleChannelTaskQueue<Uri>(
        coroutineScope = addImagesScope
    )
    private val ocrTasksChannelQueue = SimpleChannelTaskQueue<OcrQueueTask>(
        coroutineScope = ocrQueueScope
    )

    init {
        setParallelCount(parallelCount.value)

        Log.i(
            LOG_TAG,
            "Channel queues ready, addImages: ${addImagesChannelQueue.logHashCode()}, ocrTasks: ${ocrTasksChannelQueue.logHashCode()}"
        )
    }

    val queueRunning = ocrTasksChannelQueue.isRunning
    val addImagesProgress = addImagesChannelQueue.progress
    val addImagesProgressTotal = addImagesChannelQueue.progressTotal

    private var lastTaskId = 1
    private val taskIdAllocLock = Mutex()

    fun setParallelCount(parallelCount: Int) {
        _parallelCount.value = parallelCount
        addImagesChannelQueue.parallelCount = parallelCount
        ocrTasksChannelQueue.parallelCount = parallelCount
    }

    private fun emitTaskUpdated(taskId: Int) {
        MainScope().launch {
            _taskUpdatedFlow.emit(taskId)
        }
    }

    private suspend fun getNewTaskId(): Int {
        taskIdAllocLock.withLock {
            lastTaskId += 1
            return lastTaskId - 1
        }
    }

    private suspend fun getNewTask(fileUri: Uri): OcrQueueTask {
        return OcrQueueTask(
            id = getNewTaskId(),
            fileUri = fileUri,
            status = OcrQueueTaskStatus.IDLE,
        )
    }

    private fun addTask(task: OcrQueueTask) {
        if (ocrTasksChannelQueue.isRunning.value) {
            Log.e(LOG_TAG, "Cannot add task ${task.id}, queue running!")
            return
        }
        if (ocrQueueTasksMap[task.id] != null) {
            Log.e(LOG_TAG, "Cannot add task ${task.id}, task already in queue!")
            return
        }

        ocrQueueTasksMap[task.id] = task
        emitTaskUpdated(task.id)
    }

    fun editTaskScore(taskId: Int, playResult: PlayResult) {
        val task = ocrQueueTasksMap[taskId]
        if (task == null) {
            Log.e(LOG_TAG, "Cannot modify task ${taskId}, task not found!")
            return
        }

        task.playResult = playResult
        emitTaskUpdated(task.id)
    }

    fun deleteTask(taskId: Int) {
        if (ocrTasksChannelQueue.isRunning.value) {
            Log.e(LOG_TAG, "Cannot delete task ${taskId}, queue running!")
            return
        }
        if (ocrQueueTasksMap[taskId] == null) {
            Log.e(LOG_TAG, "Cannot delete task ${taskId}, task not found!")
            return
        }

        ocrQueueTasksMap.remove(taskId)
        emitTaskUpdated(taskId)
    }

    fun clear() {
        ocrQueueTasksMap.clear()
        emitTaskUpdated(-1)
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

    private suspend fun getTaskFromImageFile(
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
        if (addImagesChannelQueue.isRunning.value) return

        runBlocking {
            addImagesChannelQueue.start(uris) { uri ->
                val task = getTaskFromImageFile(
                    imageUri = uri,
                    context = context,
                    checkIsImage = checkIsImage,
                    detectScreenshot = detectScreenshot,
                )

                if (task != null) addTask(task)
            }
        }

        Runtime.getRuntime().gc()
    }

    fun stopAddImageFiles() {
        addImagesChannelQueue.stop()
    }

    private suspend fun processTask(task: OcrQueueTask, context: Context) {
        if (task.status == OcrQueueTaskStatus.DONE) return

        fun emitUpdated() {
            emitTaskUpdated(task.id)
        }

        task.status = OcrQueueTaskStatus.PROCESSING
        emitUpdated()

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
            val chart = arcaeaOfflineDatabaseRepositoryContainer.chartRepo.find(
                ocrResult.songId, ocrResult.ratingClass
            ).firstOrNull()

            task.status = OcrQueueTaskStatus.DONE
            task.ocrResult = ocrResult
            task.playResult = score
            task.chart = chart
        } catch (e: Exception) {
            task.status = OcrQueueTaskStatus.ERROR
            task.exception = e

            Log.e(LOG_TAG, "Error occurred at task ${task.id} ${task.fileUri}", e)
        }

        emitUpdated()
    }

    suspend fun startQueue(context: Context) {
        runBlocking {
            ocrTasksChannelQueue.start(ocrQueueTasksMap.values) {
                processTask(it, context)
            }
        }

        Runtime.getRuntime().gc()
    }

    fun stopQueue() {
        ocrTasksChannelQueue.stop()
    }

    companion object {
        const val LOG_TAG = "OcrQueueCore"
    }
}
