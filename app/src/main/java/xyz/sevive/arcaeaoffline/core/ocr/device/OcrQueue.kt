package xyz.sevive.arcaeaoffline.core.ocr.device

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.apache.commons.io.IOUtils
import org.opencv.core.Mat
import org.opencv.core.MatOfByte
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import org.opencv.ml.KNearest
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import org.threeten.bp.format.DateTimeFormatter
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.database.entities.Chart
import xyz.sevive.arcaeaoffline.core.database.entities.Score
import xyz.sevive.arcaeaoffline.core.database.helpers.ChartFactory
import xyz.sevive.arcaeaoffline.core.ocr.ImagePhashDatabase
import xyz.sevive.arcaeaoffline.core.ocr.device.rois.definition.DeviceAutoRoisT2
import xyz.sevive.arcaeaoffline.core.ocr.device.rois.extractor.DeviceRoisExtractor
import xyz.sevive.arcaeaoffline.core.ocr.device.rois.masker.DeviceAutoRoisMaskerT2
import xyz.sevive.arcaeaoffline.data.ArcaeaPartnerModifiers
import xyz.sevive.arcaeaoffline.data.OcrDependencyPaths
import xyz.sevive.arcaeaoffline.ui.containers.ArcaeaOfflineDatabaseRepositoryContainerImpl
import java.io.FileNotFoundException
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

    private fun addImageFile(imageUri: Uri) {
        val task = getNewTask(imageUri)
        addTask(task)
    }

    fun addImageFile(
        imageUri: Uri,
        context: Context,
        checkIsImage: Boolean = true,
        detectScreenshot: Boolean = true,
    ) {
        if (checkIsImage && !isImage(imageUri, context)) return

        if (detectScreenshot) {
            val inputStream = context.contentResolver.openInputStream(imageUri) ?: return
            val byteArray = inputStream.use { IOUtils.toByteArray(inputStream) }

            val img = Imgcodecs.imdecode(MatOfByte(*byteArray), Imgcodecs.IMREAD_COLOR)
            val imgHsv = Mat()
            Imgproc.cvtColor(img, imgHsv, Imgproc.COLOR_BGR2HSV)
            if (!ScreenshotDetect.isArcaeaScreenshot(imgHsv)) return
        }

        addImageFile(imageUri)
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
            uris.forEach {
                launch {
                    try {
                        addImageFile(
                            it,
                            context,
                            checkIsImage = checkIsImage,
                            detectScreenshot = detectScreenshot,
                        )
                    } catch (e: Exception) {
                        Log.e(LOG_TAG, "Error adding uri $it", e)
                    } finally {
                        _addImagesProgress.value += 1
                    }
                }
            }
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

    private suspend fun processTask(
        task: OcrQueueTask,
        context: Context,
        knnModel: KNearest,
        phashDatabase: ImagePhashDatabase,
        stopFlag: Boolean = false,
    ) {
        if (task.status == OcrQueueTaskStatus.DONE) return

        if (stopFlag) return

        var taskCopy = task.copy(status = OcrQueueTaskStatus.PROCESSING)
        Log.d(LOG_TAG, "Processing task ${taskCopy.id}")
        modifyTask(task)

        try {
            val inputStream = context.contentResolver.openInputStream(task.fileUri)
                ?: throw FileNotFoundException("Cannot open a input stream for ${task.fileUri}")

            val byteArray = inputStream.use { IOUtils.toByteArray(inputStream) }
            val img = Imgcodecs.imdecode(MatOfByte(*byteArray), Imgcodecs.IMREAD_COLOR)

            val rois = DeviceAutoRoisT2(img.width(), img.height())
            val extractor = DeviceRoisExtractor(rois, img)
            val masker = DeviceAutoRoisMaskerT2()
            val ocr = DeviceOcr(extractor, masker, knnModel, phashDatabase)

            val imgExif = ExifInterface(byteArray.inputStream())
            val imgExifDateTimeOriginal = imgExif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)

            val imgDate = if (imgExifDateTimeOriginal != null) {
                LocalDateTime.parse(
                    imgExifDateTimeOriginal, DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss")
                ).toInstant(ZoneOffset.UTC).epochSecond
            } else {
                null
            }

            val arcaeaPartnerModifiers = ArcaeaPartnerModifiers(context.assets)

            val ocrResult = ocr.ocr()

            if (ocrResult.songId == null) throw Exception("OCR result invalid: no `song_id`")

            val arcaeaOfflineDatabaseRepositoryContainer =
                ArcaeaOfflineDatabaseRepositoryContainerImpl(context)
            val chart = ChartFactory.getChart(
                arcaeaOfflineDatabaseRepositoryContainer, ocrResult.songId, ocrResult.ratingClass
            )

            taskCopy = taskCopy.copy(
                status = OcrQueueTaskStatus.DONE,
                ocrResult = ocrResult,
                score = ocrResult.toScore(
                    date = imgDate,
                    arcaeaPartnerModifiers = arcaeaPartnerModifiers,
                ),
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
            val ocrDependencyPaths = OcrDependencyPaths(context)
            val knnModel = KNearest.load(ocrDependencyPaths.knnModelFile.path)
            val phashDatabase = ImagePhashDatabase(ocrDependencyPaths.phashDatabaseFile.path)

            _ocrQueueTasksMap.values.forEach {
                val task = it.copy()
                launch {
                    processTask(task, context, knnModel, phashDatabase, stopFlag = !this.isActive)
                }
            }
        }

        jobHandle.join()
        _queueRunning.value = false
    }


    companion object {
        const val LOG_TAG = "OcrQueueCore"
    }
}
