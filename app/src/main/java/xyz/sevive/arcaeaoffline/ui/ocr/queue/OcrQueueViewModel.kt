package xyz.sevive.arcaeaoffline.ui.ocr.queue

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xyz.sevive.arcaeaoffline.core.calculators.calculateArcaeaScoreRange
import xyz.sevive.arcaeaoffline.core.database.entities.Chart
import xyz.sevive.arcaeaoffline.core.database.entities.Score
import xyz.sevive.arcaeaoffline.core.ocr.device.DeviceOcrResult
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
}


class OcrQueueViewModel : ViewModel() {
    private val ocrQueue = xyz.sevive.arcaeaoffline.helpers.OcrQueue()

    val ocrQueueTasksUiItems = ocrQueue.ocrQueueTasks.map { tasks ->
        tasks.map {
            OcrQueueTaskUiItem(
                id = it.id,
                fileUri = it.fileUri,
                status = it.status,
                ocrResult = it.ocrResult,
                score = it.score,
                chart = it.chart,
                exception = it.exception,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(10000L), listOf())
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
        ocrQueue.modifyTaskScore(taskId, score)
    }

    suspend fun saveTaskScore(taskId: Int, context: Context) {
        val task = ocrQueue.getTask(taskId)!!

        val scoreRepository = ArcaeaOfflineDatabaseRepositoryContainerImpl(context).scoreRepository

        task.score?.let {
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

    fun tryStopQueue(context: Context? = null) {
        ocrQueue.tryStopQueue(context)
    }

    fun startQueue(context: Context) {
        viewModelScope.launch {
            ocrQueue.startQueue(context)
        }
    }
}
