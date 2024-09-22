package xyz.sevive.arcaeaoffline.ui.screens.ocr.queue

import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import xyz.sevive.arcaeaoffline.core.database.entities.Chart
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.core.ocr.device.DeviceOcrResult
import xyz.sevive.arcaeaoffline.database.entities.OcrQueueTask
import xyz.sevive.arcaeaoffline.database.entities.OcrQueueTaskStatus
import xyz.sevive.arcaeaoffline.datastore.OcrQueuePreferencesRepository
import xyz.sevive.arcaeaoffline.helpers.ArcaeaPlayResultValidator
import xyz.sevive.arcaeaoffline.helpers.ArcaeaPlayResultValidatorWarning
import xyz.sevive.arcaeaoffline.helpers.OcrQueueEnqueueCheckerJob
import xyz.sevive.arcaeaoffline.helpers.OcrQueueJob
import xyz.sevive.arcaeaoffline.ui.containers.ArcaeaOfflineDatabaseRepositoryContainer
import xyz.sevive.arcaeaoffline.ui.containers.OcrQueueDatabaseRepositoryContainer
import kotlin.time.Duration.Companion.seconds


class OcrQueueScreenViewModel(
    private val workManager: WorkManager,
    private val repositoryContainer: ArcaeaOfflineDatabaseRepositoryContainer,
    ocrQueueRepos: OcrQueueDatabaseRepositoryContainer,
    private val preferencesRepository: OcrQueuePreferencesRepository,
) : ViewModel() {
    companion object {
        const val LOG_TAG = "OcrQueueScreenViewModel"
    }

    private val ocrQueueTaskRepo = ocrQueueRepos.ocrQueueTaskRepo
    private val ocrQueueEnqueueBufferRepo = ocrQueueRepos.ocrQueueEnqueueBufferRepo

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

    data class TaskUiItem(
        val id: Long,
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

        constructor(
            dbItem: OcrQueueTask,
            chart: Chart? = null,
        ) : this(
            id = dbItem.id,
            fileUri = dbItem.fileUri,
            status = dbItem.status,
            ocrResult = dbItem.result,
            playResult = dbItem.playResult,
            chart = chart,
            exception = dbItem.exception,
        )
    }

    data class EnqueueCheckerJobUiState(
        val isPreparing: Boolean = false,
        val isRunning: Boolean = false,
        val progress: Pair<Int, Int>? = null,
    )

    val queueRunning = workManager.getWorkInfosForUniqueWorkFlow(OcrQueueJob.WORK_NAME).map {
        it != null && it.isNotEmpty() && it[0].state == androidx.work.WorkInfo.State.RUNNING
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(2500L), false)

    val totalCount =
        ocrQueueTaskRepo.count().stateIn(viewModelScope, SharingStarted.WhileSubscribed(2500L), 0)
    val idleCount = ocrQueueTaskRepo.countByStatus(OcrQueueTaskStatus.IDLE)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(2500L), 0)
    val processingCount = ocrQueueTaskRepo.countByStatus(OcrQueueTaskStatus.PROCESSING)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(2500L), 0)
    val doneCount = ocrQueueTaskRepo.countByStatus(OcrQueueTaskStatus.DONE)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(2500L), 0)
    val doneWithWarningCount = ocrQueueTaskRepo.countDoneWithWarning()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(2500L), 0)
    val errorCount = ocrQueueTaskRepo.countByStatus(OcrQueueTaskStatus.ERROR)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(2500L), 0)

    fun deleteTask(taskId: Long) {
        if (queueRunning.value) return

        viewModelScope.launch(Dispatchers.IO) { ocrQueueTaskRepo.delete(taskId) }
    }

    fun clearTasks() {
        viewModelScope.launch(Dispatchers.IO) { ocrQueueTaskRepo.deleteAll() }
    }

    fun modifyTaskScore(taskId: Long, playResult: PlayResult) {
        viewModelScope.launch(Dispatchers.IO) {
            ocrQueueTaskRepo.updatePlayResult(taskId, playResult)
        }
    }

    fun saveTaskScore(taskId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            ocrQueueTaskRepo.save(taskId, repositoryContainer.playResultRepo)
        }
    }

    fun saveAllTaskPlayResults() {
        if (queueRunning.value) return

        viewModelScope.launch(Dispatchers.IO) {
            ocrQueueTaskRepo.saveAll(repositoryContainer.playResultRepo)
        }
    }

    private val enqueueCheckerJobWorkInfo =
        workManager.getWorkInfosForUniqueWorkFlow(OcrQueueEnqueueCheckerJob.WORK_NAME).map {
            it.getOrNull(0)
        }
    private val enqueueCheckerIsPreparing = MutableStateFlow(false)
    private val enqueueCheckerProgress = combine(
        enqueueCheckerJobWorkInfo,
        ocrQueueEnqueueBufferRepo.countChecked(),
        ocrQueueEnqueueBufferRepo.count(),
    ) { workInfo, p, t ->
        if (workInfo?.state == WorkInfo.State.RUNNING && t > 0) Pair(p, t)
        else if (workInfo?.state == WorkInfo.State.ENQUEUED) Pair(0, -1)
        else null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)
    val enqueueCheckerJobUiState = combine(
        enqueueCheckerJobWorkInfo,
        enqueueCheckerIsPreparing,
        enqueueCheckerProgress,
    ) { workInfo, isPreparing, progress ->
        EnqueueCheckerJobUiState(
            isPreparing = isPreparing,
            isRunning = workInfo?.state == WorkInfo.State.RUNNING,
            progress = progress
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), EnqueueCheckerJobUiState())

    fun addImageFiles(uris: List<Uri>) {
        viewModelScope.launch(Dispatchers.IO) {
            ocrQueueEnqueueBufferRepo.insertBatch(uris)

            val workRequest = OneTimeWorkRequestBuilder<OcrQueueEnqueueCheckerJob>().setInputData(
                Data.Builder().putBoolean(
                    OcrQueueEnqueueCheckerJob.KEY_INPUT_CHECK_IS_IMAGE,
                    preferencesUiState.value.checkIsImage
                ).putBoolean(
                    OcrQueueEnqueueCheckerJob.KEY_INPUT_CHECK_IS_ARCAEA_IMAGE,
                    preferencesUiState.value.checkIsArcaeaImage
                ).putInt(
                    OcrQueueEnqueueCheckerJob.KEY_PARALLEL_COUNT,
                    preferencesUiState.value.parallelCount
                ).build()
            ).setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)

            workManager.enqueueUniqueWork(
                OcrQueueEnqueueCheckerJob.WORK_NAME, ExistingWorkPolicy.REPLACE, workRequest.build()
            )
        }
    }

    fun addFolder(folder: DocumentFile) {
        viewModelScope.launch(Dispatchers.IO) {
            enqueueCheckerIsPreparing.value = true
            try {
                val uris = folder.listFiles().filter { it.isFile }.map { it.uri }
                addImageFiles(uris)
            } finally {
                enqueueCheckerIsPreparing.value = false
            }
        }
    }

    fun stopAddImageFiles() {
        workManager.cancelUniqueWork(OcrQueueEnqueueCheckerJob.WORK_NAME)
    }

    fun tryStopQueue() {
        workManager.cancelUniqueWork(OcrQueueJob.WORK_NAME)
    }

    fun startQueue(runMode: OcrQueueJob.RunMode = OcrQueueJob.RunMode.NORMAL) {
        viewModelScope.launch(Dispatchers.Default) {
            val workRequest =
                OneTimeWorkRequestBuilder<OcrQueueJob>().setExpedited(OutOfQuotaPolicy.DROP_WORK_REQUEST)
                    .setInputData(
                        Data.Builder().putInt(
                            OcrQueueJob.DATA_RUN_MODE,
                            runMode.value,
                        ).putInt(
                            OcrQueueJob.DATA_PARALLEL_COUNT,
                            preferencesUiState.value.parallelCount,
                        ).build()
                    ).build()

            workManager.enqueueUniqueWork(
                OcrQueueJob.WORK_NAME, ExistingWorkPolicy.KEEP, workRequest
            )
        }
    }

    private val _currentScreenCategory = MutableStateFlow(OcrQueueScreenCategory.NULL)
    internal val currentScreenCategory = _currentScreenCategory.asStateFlow()

    internal fun setCurrentScreenCategory(category: OcrQueueScreenCategory) {
        _currentScreenCategory.value = category
    }

    private val _currentUiItemsLoading = MutableStateFlow(true)
    val currentUiItemsLoading = _currentUiItemsLoading.asStateFlow()

    private suspend fun mapDbItemsToUiItems(dbItems: List<OcrQueueTask>): List<TaskUiItem> {
        val cMap = mutableMapOf<String, Chart>()
        val songIds = dbItems.mapNotNull { it.playResult?.songId }.distinct()
        val charts = repositoryContainer.chartRepo.findAllBySongIds(songIds).firstOrNull()
            ?: return emptyList()
        charts.forEach { cMap["${it.songId}|${it.ratingClass}"] = it }

        return dbItems.map {
            if (it.playResult == null) TaskUiItem(dbItem = it)
            else TaskUiItem(
                dbItem = it, chart = cMap["${it.playResult.songId}|${it.playResult.ratingClass}"]
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentUiItems = _currentScreenCategory.flatMapLatest {
        // first, map category to db items
        when (it) {
            OcrQueueScreenCategory.NULL -> emptyFlow()
            OcrQueueScreenCategory.DONE_WITH_WARNING -> ocrQueueTaskRepo.findDoneWithWarning()

            else -> {
                val taskStatus = when (it) {
                    OcrQueueScreenCategory.IDLE -> OcrQueueTaskStatus.IDLE
                    OcrQueueScreenCategory.PROCESSING -> OcrQueueTaskStatus.PROCESSING
                    OcrQueueScreenCategory.DONE -> OcrQueueTaskStatus.DONE
                    OcrQueueScreenCategory.ERROR -> OcrQueueTaskStatus.ERROR
                    else -> throw IllegalStateException("Category not implemented: $it")
                }
                ocrQueueTaskRepo.findByStatus(taskStatus)
            }
        }
    }.mapLatest {
        // then, map db items to ui items
        _currentUiItemsLoading.value = true
        val uiItems = mapDbItemsToUiItems(it)
        _currentUiItemsLoading.value = false

        uiItems
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(1.seconds.inWholeMilliseconds),
        emptyList(),
    )
}
