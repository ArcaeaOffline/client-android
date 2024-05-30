package xyz.sevive.arcaeaoffline.ui.ocr.queue

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xyz.sevive.arcaeaoffline.core.database.entities.Chart
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.core.ocr.device.DeviceOcrResult
import xyz.sevive.arcaeaoffline.helpers.ArcaeaPlayResultValidator
import xyz.sevive.arcaeaoffline.helpers.ArcaeaPlayResultValidatorWarning
import xyz.sevive.arcaeaoffline.helpers.OcrQueueTask
import xyz.sevive.arcaeaoffline.helpers.OcrQueueTaskStatus
import xyz.sevive.arcaeaoffline.ui.containers.ArcaeaOfflineDatabaseRepositoryContainerImpl


data class OcrQueueTaskUiItem(
    val id: Int,
    var fileUri: Uri,
    var status: OcrQueueTaskStatus,
    var ocrResult: DeviceOcrResult? = null,
    var playResult: PlayResult? = null,
    var chart: Chart? = null,
    var exception: Exception? = null,
) {
    fun scoreValidatorWarnings(): List<ArcaeaPlayResultValidatorWarning> {
        return playResult?.let {
            ArcaeaPlayResultValidator.validateScore(it, chart)
        } ?: emptyList()
    }

    companion object {
        fun fromTask(task: OcrQueueTask): OcrQueueTaskUiItem {
            return OcrQueueTaskUiItem(
                id = task.id,
                fileUri = task.fileUri,
                status = task.status,
                ocrResult = task.ocrResult,
                playResult = task.playResult,
                chart = task.chart,
                exception = task.exception,
            )
        }
    }
}

class OcrQueueViewModel(
    private val preferencesRepository: OcrQueuePreferencesRepository
) : ViewModel() {
    companion object {
        const val LOG_TAG = "OcrQueueViewModel"
    }

    private val ocrQueue = xyz.sevive.arcaeaoffline.helpers.OcrQueue()

    // #region Preferences
    private val _checkIsImage = MutableStateFlow(true)
    val checkIsImage = _checkIsImage.asStateFlow()

    private val _checkIsArcaeaImage = MutableStateFlow(true)
    val checkIsArcaeaImage = _checkIsArcaeaImage.asStateFlow()

    val channelCapacity = ocrQueue.channelCapacity
    val parallelCount = ocrQueue.parallelCount

    val parallelCountMin = 2
    val parallelCountMax = Runtime.getRuntime().availableProcessors() * 4

    /*
     * Instead of directly modifying the preferences values, they're handled
     * by the `referencesFlow` below.
     *
     * All these `set*` functions only triggers a save to the preferences (data store),
     * then the `preferencesFlow` is collected to reflect the changes.
     */
    suspend fun setCheckIsImage(value: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setCheckIsImage(value)
        }
    }

    suspend fun setCheckIsArcaeaImage(value: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setCheckIsArcaeaImage(value)
        }
    }

    suspend fun setChannelCapacity(value: Int) {
        viewModelScope.launch {
            preferencesRepository.setChannelCapacity(value)
        }
    }

    suspend fun setParallelCount(value: Int) {
        viewModelScope.launch {
            preferencesRepository.setParallelCount(value)
        }
    }

    private val preferencesFlow = preferencesRepository.preferencesFlow

    init {
        viewModelScope.launch {
            preferencesFlow.collect {
                _checkIsImage.value = it.checkIsImage
                _checkIsArcaeaImage.value = it.checkIsArcaeaImage

                if (it.channelCapacity != null) {
                    ocrQueue.setChannelCapacity(it.channelCapacity)
                }

                if (it.parallelCount != null) {
                    ocrQueue.setParallelCount(it.parallelCount)
                }
            }
        }
    }
    // #endregion

    private val _uiItems = MutableStateFlow<List<OcrQueueTaskUiItem>>(emptyList())
    val uiItems = _uiItems.asStateFlow()

    private var lastUpdateTimeMillis = System.currentTimeMillis()
    private fun updateUiItems() {
        _uiItems.value = ocrQueue.ocrQueueTasksMap.values.map {
            OcrQueueTaskUiItem.fromTask(it)
        }
        lastUpdateTimeMillis = System.currentTimeMillis()
    }


    private var lastDelayUpdateJob: Job? = null
    private val uiItemsUpdateIntervalMillis = 300L

    init {
        viewModelScope.launch {
            ocrQueue.taskUpdatedFlow.collect {
                // Once a new value is collected, we check if the ui items update is needed.

                // Firstly, if the last update interval is long enough, update immediately.
                if (System.currentTimeMillis() - lastUpdateTimeMillis >= uiItemsUpdateIntervalMillis) {
                    updateUiItems()
                    Log.v(LOG_TAG, "uiItemsUpdate: interval update at $lastUpdateTimeMillis")
                    return@collect
                }

                // Otherwise, request a debounced update, so the latest value is ensured to be shown.
                // See this SO answer https://stackoverflow.com/a/57252799/16484891
                // CC BY-SA 4.0
                lastDelayUpdateJob?.cancel()
                lastDelayUpdateJob = viewModelScope.launch {
                    delay(uiItemsUpdateIntervalMillis)
                    if (this.isActive) {
                        updateUiItems()
                        Log.v(LOG_TAG, "uiItemsUpdate: delayed update at $lastUpdateTimeMillis")
                    }
                }
            }
        }
    }

    val queueRunning = ocrQueue.queueRunning

    private val _addImagesFromFolderProcessing = MutableStateFlow(false)
    val addImagesFromFolderProcessing = _addImagesFromFolderProcessing.asStateFlow()
    val addImagesProgress = ocrQueue.addImagesProgress
    val addImagesProgressTotal = ocrQueue.addImagesProgressTotal

    fun deleteTask(taskId: Int) {
        ocrQueue.deleteTask(taskId)
    }

    fun clearTasks() {
        ocrQueue.clear()
    }

    fun modifyTaskScore(taskId: Int, playResult: PlayResult) {
        ocrQueue.editTaskScore(taskId, playResult)
    }

    suspend fun saveTaskScore(taskId: Int, context: Context) {
        val uiItem = uiItems.value.find { it.id == taskId } ?: return

        val scoreRepository = ArcaeaOfflineDatabaseRepositoryContainerImpl(context).playResultRepo

        uiItem.playResult?.let {
            scoreRepository.upsert(it)
            ocrQueue.deleteTask(taskId)
        }
    }

    suspend fun addImageFiles(uris: List<Uri>, context: Context) {
        ocrQueue.addImageFiles(
            uris,
            context,
            checkIsImage = checkIsImage.value,
            detectScreenshot = checkIsArcaeaImage.value,
        )
    }

    suspend fun addFolder(folder: DocumentFile, context: Context) {
        _addImagesFromFolderProcessing.value = true
        withContext(Dispatchers.IO) {
            val uris = folder.listFiles().filter { it.isFile }.map { it.uri }
            addImageFiles(uris, context)
        }
        _addImagesFromFolderProcessing.value = false
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
