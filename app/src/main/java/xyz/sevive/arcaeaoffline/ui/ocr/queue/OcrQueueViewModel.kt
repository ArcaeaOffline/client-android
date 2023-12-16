package xyz.sevive.arcaeaoffline.ui.ocr.queue

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import androidx.exifinterface.media.ExifInterface
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
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import org.threeten.bp.format.DateTimeFormatter
import xyz.sevive.arcaeaoffline.core.calculate.calculateArcaeaScoreRange
import xyz.sevive.arcaeaoffline.core.database.entities.Chart
import xyz.sevive.arcaeaoffline.core.database.entities.Score
import xyz.sevive.arcaeaoffline.core.database.helpers.ChartFactory
import xyz.sevive.arcaeaoffline.core.ocr.ImagePhashDatabase
import xyz.sevive.arcaeaoffline.core.ocr.device.DeviceOcr
import xyz.sevive.arcaeaoffline.core.ocr.device.DeviceOcrResult
import xyz.sevive.arcaeaoffline.core.ocr.device.ScreenshotDetect
import xyz.sevive.arcaeaoffline.core.ocr.device.rois.definition.DeviceAutoRoisT2
import xyz.sevive.arcaeaoffline.core.ocr.device.rois.extractor.DeviceRoisExtractor
import xyz.sevive.arcaeaoffline.core.ocr.device.rois.masker.DeviceAutoRoisMaskerT2
import xyz.sevive.arcaeaoffline.core.ocr.device.toScore
import xyz.sevive.arcaeaoffline.data.ArcaeaPartnerModifiers
import xyz.sevive.arcaeaoffline.data.OcrDependencyPaths
import xyz.sevive.arcaeaoffline.ui.containers.ArcaeaOfflineDatabaseRepositoryContainerImpl
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
    var chart: Chart? = null,
    var exception: Exception? = null,
) {
    val scoreValid: Boolean
        get() {
            val chart = chart?.copy()
            val score = score?.copy()
            return if (chart?.notes == null || score == null || score.pure == null || score.far == null) {
                false
            } else {
                calculateArcaeaScoreRange(chart.notes, score.pure, score.far).contains(score.score)
            }
        }
}

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

    private fun checkTask(taskId: Int): Boolean {
        return _ocrQueueTasksMap.contains(taskId)
    }

    private fun checkTaskOrLog(taskId: Int): Boolean {
        val result = checkTask(taskId)
        if (!result) {
            Log.w(LogTag, "Cannot find task $taskId")
        }
        return result
    }

    private fun modifyTask(task: OcrQueueTask) {
        if (!checkTaskOrLog(task.id)) return

        _ocrQueueTasksMap[task.id] = task
        syncQueueStateFlow()
    }

    fun deleteTask(taskId: Int) {
        if (queueRunning.value) {
            Log.w(LogTag, "Cannot add/delete task during queue running!")
            return
        }

        if (!checkTaskOrLog(taskId)) return

        _ocrQueueTasksMap.remove(taskId)
        syncQueueStateFlow()
    }

    fun editScore(taskId: Int, score: Score) {
        if (!checkTaskOrLog(taskId)) return

        val task = _ocrQueueTasksMap[taskId]!!.copy(score = score)
        modifyTask(task)
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

    private fun addImageFileOrFail(imageUri: Uri, context: Context) {
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

    private suspend fun processTask(
        task: OcrQueueTask,
        context: Context,
        knnModel: KNearest,
        phashDatabase: ImagePhashDatabase,
    ) {
        if (task.status == OcrQueueStatus.DONE) return

        var taskCopy = task.copy(status = OcrQueueStatus.PROCESSING)
        Log.d(LogTag, "Processing task ${taskCopy.id}")
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
                arcaeaOfflineDatabaseRepositoryContainer,
                ocrResult.songId,
                ocrResult.ratingClass
            )

            taskCopy = taskCopy.copy(
                status = OcrQueueStatus.DONE,
                ocrResult = ocrResult,
                score = ocrResult.toScore(
                    date = imgDate,
                    arcaeaPartnerModifiers = arcaeaPartnerModifiers,
                ),
                chart = chart,
            )
        } catch (e: Exception) {
            taskCopy = taskCopy.copy(
                status = OcrQueueStatus.ERROR,
                exception = e,
            )
            Log.e(LogTag, "Error occurred at task ${task.id} ${task.fileUri}", e)
        }

        modifyTask(taskCopy)
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
