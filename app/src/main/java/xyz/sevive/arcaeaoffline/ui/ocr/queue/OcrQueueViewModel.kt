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
import xyz.sevive.arcaeaoffline.datastore.OcrQueuePreferencesRepository
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
    private val preferencesRepository: OcrQueuePreferencesRepository,
) : ViewModel() {
    companion object {
        const val LOG_TAG = "OcrQueueViewModel"
    }

    private val ocrQueue = xyz.sevive.arcaeaoffline.helpers.OcrQueue()

    // #region Preferences
    data class PreferencesUiState(
        val checkIsImage: Boolean = false,
        val checkIsArcaeaImage: Boolean = false,
        val parallelCount: Int = -1,

        val parallelCountMin: Int = 2,
        val parallelCountMax: Int = Runtime.getRuntime().availableProcessors() * 4,
    )

    private val _preferencesUiState = MutableStateFlow(PreferencesUiState())
    val preferencesUiState = _preferencesUiState.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesRepository.preferencesFlow.collect {
                ocrQueue.setParallelCount(it.parallelCount)

                _preferencesUiState.value = PreferencesUiState(
                    checkIsImage = it.checkIsImage,
                    checkIsArcaeaImage = it.checkIsArcaeaImage,
                    parallelCount = it.parallelCount,
                )
            }
        }
    }

    fun setCheckIsImage(value: Boolean) {
        viewModelScope.launch { preferencesRepository.setCheckIsImage(value) }
    }

    fun setCheckIsArcaeaImage(value: Boolean) {
        viewModelScope.launch { preferencesRepository.setCheckIsArcaeaImage(value) }
    }

    fun setParallelCount(value: Int) {
        // `ocrQueue.setParallelCount(value)` is handled in the upper
        // `preferencesFlow.collect {...}`.
        viewModelScope.launch { preferencesRepository.setParallelCount(value) }
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
            checkIsImage = preferencesUiState.value.checkIsImage,
            detectScreenshot = preferencesUiState.value.checkIsArcaeaImage,
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
