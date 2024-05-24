package xyz.sevive.arcaeaoffline.ui.ocr.queue

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xyz.sevive.arcaeaoffline.core.calculators.calculateArcaeaScoreRange
import xyz.sevive.arcaeaoffline.core.database.entities.Chart
import xyz.sevive.arcaeaoffline.core.database.entities.Score
import xyz.sevive.arcaeaoffline.core.ocr.device.DeviceOcrResult
import xyz.sevive.arcaeaoffline.helpers.OcrQueueTask
import xyz.sevive.arcaeaoffline.helpers.OcrQueueTaskStatus
import xyz.sevive.arcaeaoffline.ui.containers.ArcaeaOfflineDatabaseRepositoryContainerImpl


data class OcrQueueTaskUiItem(
    val id: Int,
    var fileUri: Uri,
    var status: OcrQueueTaskStatus,
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
                calculateArcaeaScoreRange(
                    chart.notes!!,
                    score.pure!!,
                    score.far!!
                ).contains(score.score)
            }
        }

    companion object {
        fun fromTask(task: OcrQueueTask): OcrQueueTaskUiItem {
            return OcrQueueTaskUiItem(
                id = task.id,
                fileUri = task.fileUri,
                status = task.status,
                ocrResult = task.ocrResult,
                score = task.score,
                chart = task.chart,
                exception = task.exception,
            )
        }
    }
}


class OcrQueueViewModel : ViewModel() {
    private val ocrQueue = xyz.sevive.arcaeaoffline.helpers.OcrQueue()

    private val _uiItems = MutableStateFlow<List<OcrQueueTaskUiItem>>(emptyList())
    val uiItems = _uiItems.asStateFlow()

    init {
        viewModelScope.launch {
            ocrQueue.taskUpdatedFlow.collect {
                updateUiItems()
            }
        }
    }

    private fun updateUiItems() {
        _uiItems.value = ocrQueue.ocrQueueTasksMap.values.map {
            OcrQueueTaskUiItem.fromTask(it)
        }
    }

    val queueRunning = ocrQueue.queueRunning

    private val _addImagesProcessing = MutableStateFlow(false)
    val addImagesProcessing = _addImagesProcessing.asStateFlow()
    val addImagesProgress = ocrQueue.addImagesProgress
    val addImagesProgressTotal = ocrQueue.addImagesProgressTotal

    private val _checkIsImage = MutableStateFlow(true)
    val checkIsImage = _checkIsImage.asStateFlow()

    private val _detectScreenshot = MutableStateFlow(true)
    val detectScreenshot = _detectScreenshot.asStateFlow()

    fun setCheckIsImage(value: Boolean) {
        _checkIsImage.value = value
    }

    fun setDetectScreenshot(value: Boolean) {
        _detectScreenshot.value = value
    }

    fun deleteTask(taskId: Int) {
        ocrQueue.deleteTask(taskId)
    }

    fun clearTasks() {
        ocrQueue.clear()
    }

    fun modifyTaskScore(taskId: Int, score: Score) {
        ocrQueue.editTaskScore(taskId, score)
    }

    suspend fun saveTaskScore(taskId: Int, context: Context) {
        val uiItem = uiItems.value.find { it.id == taskId } ?: return

        val scoreRepository = ArcaeaOfflineDatabaseRepositoryContainerImpl(context).scoreRepository

        uiItem.score?.let {
            scoreRepository.upsert(it)
            ocrQueue.deleteTask(taskId)
        }
    }

    private suspend fun addImageFilesToQueue(uris: List<Uri>, context: Context) {
        ocrQueue.addImageFiles(
            uris,
            context,
            checkIsImage = checkIsImage.value,
            detectScreenshot = detectScreenshot.value,
        )
    }

    suspend fun addImageFiles(uris: List<Uri>, context: Context) {
        _addImagesProcessing.value = true
        addImageFilesToQueue(uris, context)
        _addImagesProcessing.value = false
    }

    suspend fun addFolder(folder: DocumentFile, context: Context) {
        _addImagesProcessing.value = true
        withContext(Dispatchers.IO) {
            val uris = folder.listFiles().filter { it.isFile }.map { it.uri }
            addImageFiles(uris, context)
        }
        _addImagesProcessing.value = false
    }

    fun stopAddImageFiles() {
        ocrQueue.stopAddImageFiles()
    }

    fun tryStopQueue() {
        ocrQueue.stopQueue()
    }

    suspend fun startQueue(context: Context) {
        withContext(Dispatchers.Default) {
            ocrQueue.startQueue(context)
        }
    }
}
