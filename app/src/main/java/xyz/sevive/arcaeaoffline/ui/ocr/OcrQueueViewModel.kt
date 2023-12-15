package xyz.sevive.arcaeaoffline.ui.ocr

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.apache.commons.io.IOUtils
import org.opencv.core.Mat
import org.opencv.core.MatOfByte
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import org.opencv.ml.KNearest
import xyz.sevive.arcaeaoffline.core.database.entities.Score
import xyz.sevive.arcaeaoffline.core.ocr.ImagePhashDatabase
import xyz.sevive.arcaeaoffline.core.ocr.device.DeviceOcr
import xyz.sevive.arcaeaoffline.core.ocr.device.DeviceOcrResult
import xyz.sevive.arcaeaoffline.core.ocr.device.ScreenshotDetect
import xyz.sevive.arcaeaoffline.core.ocr.device.rois.definition.DeviceAutoRoisT2
import xyz.sevive.arcaeaoffline.core.ocr.device.rois.extractor.DeviceRoisExtractor
import xyz.sevive.arcaeaoffline.core.ocr.device.rois.masker.DeviceAutoRoisMaskerT2
import xyz.sevive.arcaeaoffline.core.ocr.device.toScore
import xyz.sevive.arcaeaoffline.data.OcrDependencyPaths
import java.io.FileNotFoundException
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock


enum class OcrQueueStatus { IDLE, PROCESSING, DONE, ERROR }

data class OcrQueueTask(
    val id: Int,
    var fileUri: Uri,
    var status: OcrQueueStatus,
    var ocrResult: DeviceOcrResult? = null,
    var score: Score? = null,
    var exception: Exception? = null,
)

class OcrQueueViewModel : ViewModel() {
    private val _ocrQueueTasksMap = mutableMapOf<Int, OcrQueueTask>()
    private val _ocrQueueTasks = MutableStateFlow(listOf<OcrQueueTask>())
    val ocrQueueTasks = _ocrQueueTasks.asStateFlow()
    private val queueStateFlowSyncLock = ReentrantLock()

    private val _queueRunning = MutableStateFlow(false)
    val queueRunning = _queueRunning.asStateFlow()
    private val _stopQueueFlag = MutableStateFlow(false)
    val stopQueueFlag = _stopQueueFlag.asStateFlow()

    private val _addImagesProgress = MutableStateFlow(-1)
    val addImagesProgress = _addImagesProgress.asStateFlow()
    private val _addImagesProgressTotal = MutableStateFlow(-1)
    val addImagesProgressTotal = _addImagesProgressTotal.asStateFlow()

    private fun getNewTaskId(): Int {
        val ids = _ocrQueueTasksMap.keys
        return if (ids.isEmpty()) 0 else ids.max() + 1
    }

    private fun getNewTask(fileUri: Uri): OcrQueueTask {
        return OcrQueueTask(
            id = getNewTaskId(),
            fileUri = fileUri,
            status = OcrQueueStatus.IDLE,
        )
    }

    private fun syncQueueStateFlow() {
        queueStateFlowSyncLock.withLock {
            _ocrQueueTasks.value = _ocrQueueTasksMap.values.toList()
        }
    }

    private fun addTask(task: OcrQueueTask) {
        if (queueRunning.value) {
            Log.w(LogTag, "Cannot add/delete task during queue running!")
            return
        }

        val taskInList = _ocrQueueTasksMap.keys.find { it == task.id } != null
        if (taskInList) throw IllegalArgumentException("OcrQueueTask(id=${task.id}) already exists!")

        _ocrQueueTasksMap[task.id] = task
        syncQueueStateFlow()
    }

    private fun modifyTask(task: OcrQueueTask) {
        _ocrQueueTasksMap[task.id] = task
        syncQueueStateFlow()
    }

    private fun deleteTask(task: OcrQueueTask) {
        if (queueRunning.value) {
            Log.w(LogTag, "Cannot add/delete task during queue running!")
            return
        }

        _ocrQueueTasksMap.remove(task.id)
        syncQueueStateFlow()
    }

    fun deleteTask(taskId: Int) {
        val task = _ocrQueueTasksMap[taskId]
        if (task == null) {
            Log.w(LogTag, "Cannot delete task $taskId, task not found!")
            return
        }

        deleteTask(task)
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

    fun addImageFileOrFail(imageUri: Uri, context: Context) {
        if (!isImage(imageUri, context)) throw IllegalArgumentException("File is not an image!")

        addImageFile(imageUri)
    }

    fun addImageFile(imageUri: Uri, context: Context) {
        try {
            addImageFileOrFail(imageUri, context)
        } catch (e: IllegalArgumentException) {
            return
        }
    }

    suspend fun addImageFiles(
        uris: List<Uri>,
        context: Context,
        detectScreenshot: Boolean = true,
    ) {
        _addImagesProgress.value = 0
        _addImagesProgressTotal.value = uris.size

        withContext(Dispatchers.IO) {
            for (uri in uris) {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri) ?: continue
                    val byteArray = inputStream.use { IOUtils.toByteArray(inputStream) }
                    if (!isImage(byteArray)) continue

                    if (detectScreenshot) {
                        val img = Imgcodecs.imdecode(MatOfByte(*byteArray), Imgcodecs.IMREAD_COLOR)
                        val imgHsv = Mat()
                        Imgproc.cvtColor(img, imgHsv, Imgproc.COLOR_BGR2HSV)
                        if (!ScreenshotDetect.isArcaeaScreenshot(imgHsv)) continue
                    }

                    addImageFile(uri)
                } catch (e: Exception) {
                    Log.e(LogTag, "Error adding image $uri", e)
                } finally {
                    _addImagesProgress.value += 1
                }
            }
        }

        _addImagesProgress.value = -1
        _addImagesProgressTotal.value = -1
    }

    suspend fun addFolder(
        folder: DocumentFile,
        context: Context,
        detectScreenshot: Boolean = true,
    ) {
        val uris = folder.listFiles().filter { it.isFile }.map { it.uri }
        addImageFiles(uris, context, detectScreenshot)
    }

    private fun processTask(
        task: OcrQueueTask,
        context: Context,
        knnModel: KNearest,
        phashDatabase: ImagePhashDatabase,
    ) {
        if (task.status == OcrQueueStatus.DONE) return

        Log.d(LogTag, "Processing task ${task.id}")

        task.status = OcrQueueStatus.PROCESSING
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

            // TODO: read image exif
            // TODO: build ArcaeaPartnerModifiers
            val ocrResult = ocr.ocr()
            task.status = OcrQueueStatus.DONE
            task.ocrResult = ocrResult
            task.score = ocrResult.toScore()
        } catch (e: Exception) {
            task.status = OcrQueueStatus.ERROR
            task.exception = e
        }

        modifyTask(task)
    }

    fun tryStopQueue() {
        if (!queueRunning.value) {
            Log.w(LogTag, "Queue hasn't started!")
            return
        }

        _stopQueueFlag.value = true
    }

    suspend fun startQueue(context: Context) {
        if (queueRunning.value) {
            Log.w(LogTag, "Queue has already started!")
            return
        } else _queueRunning.value = true

        withContext(Dispatchers.IO) {
            val ocrDependencyPaths = OcrDependencyPaths(context)
            val knnModel = KNearest.load(ocrDependencyPaths.knnModelFile.path)
            val phashDatabase = ImagePhashDatabase(ocrDependencyPaths.phashDatabaseFile.path)

            for (originalTask in _ocrQueueTasksMap.values) {
                if (_stopQueueFlag.value) break

                val task = originalTask.copy()
                processTask(task, context, knnModel, phashDatabase)
            }

            _queueRunning.value = false
            _stopQueueFlag.value = false
        }
    }

    companion object {
        const val LogTag = "OcrQueue"
    }
}
