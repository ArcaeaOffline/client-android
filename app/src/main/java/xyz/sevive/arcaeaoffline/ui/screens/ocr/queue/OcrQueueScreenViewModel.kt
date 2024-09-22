package xyz.sevive.arcaeaoffline.ui.screens.ocr.queue

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import xyz.sevive.arcaeaoffline.core.database.entities.Chart
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.core.ocr.device.DeviceOcrResult
import xyz.sevive.arcaeaoffline.database.entities.OcrQueueTask
import xyz.sevive.arcaeaoffline.database.entities.OcrQueueTaskStatus
import xyz.sevive.arcaeaoffline.datastore.OcrQueuePreferencesRepository
import xyz.sevive.arcaeaoffline.datastore.OcrQueuePreferencesSerializer
import xyz.sevive.arcaeaoffline.helpers.ArcaeaPlayResultValidator
import xyz.sevive.arcaeaoffline.helpers.ArcaeaPlayResultValidatorWarning
import xyz.sevive.arcaeaoffline.helpers.OcrQueueJob
import xyz.sevive.arcaeaoffline.ui.containers.ArcaeaOfflineDatabaseRepositoryContainer
import xyz.sevive.arcaeaoffline.ui.containers.OcrQueueDatabaseRepositoryContainer
import kotlin.time.Duration.Companion.seconds


class OcrQueueScreenViewModel(
    private val workManager: WorkManager,
    private val repositoryContainer: ArcaeaOfflineDatabaseRepositoryContainer,
    ocrQueueRepos: OcrQueueDatabaseRepositoryContainer,
    preferencesRepository: OcrQueuePreferencesRepository,
) : ViewModel() {
    companion object {
        const val LOG_TAG = "OcrQueueScreenViewModel"
    }

    private val ocrQueueTaskRepo = ocrQueueRepos.ocrQueueTaskRepo

    private val ocrQueuePreferences = preferencesRepository.preferencesFlow.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        OcrQueuePreferencesSerializer.defaultValue,
    )

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

    data class QueueTaskCounts(
        val total: Int = 0,
        val idle: Int = 0,
        val processing: Int = 0,
        val done: Int = 0,
        val doneWithWarning: Int = 0,
        val error: Int = 0,
    )

    data class QueueStatusUiState(
        val isRunning: Boolean = true,
    )

    val queueTaskCounts = combine(
        ocrQueueTaskRepo.count(),
        ocrQueueTaskRepo.countByStatus(OcrQueueTaskStatus.IDLE),
        ocrQueueTaskRepo.countByStatus(OcrQueueTaskStatus.PROCESSING),
        ocrQueueTaskRepo.countByStatus(OcrQueueTaskStatus.DONE),
        ocrQueueTaskRepo.countDoneWithWarning(),
        ocrQueueTaskRepo.countByStatus(OcrQueueTaskStatus.ERROR),
    ) {
        QueueTaskCounts(
            total = it[0],
            idle = it[1],
            processing = it[2],
            done = it[3],
            doneWithWarning = it[4],
            error = it[5],
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5.seconds.inWholeMilliseconds),
        QueueTaskCounts(),
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val queueStatusUiState = workManager.getWorkInfosForUniqueWorkFlow(OcrQueueJob.WORK_NAME).map {
        it.getOrNull(0)
    }.mapLatest {
        QueueStatusUiState(
            isRunning = it?.state == androidx.work.WorkInfo.State.RUNNING,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5.seconds.inWholeMilliseconds),
        QueueStatusUiState(),
    )

    private val queueRunning =
        workManager.getWorkInfosForUniqueWorkFlow(OcrQueueJob.WORK_NAME).map {
            it != null && it.isNotEmpty() && it[0].state == androidx.work.WorkInfo.State.RUNNING
        }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

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
                            ocrQueuePreferences.value.parallelCount,
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
    val currentUiItems = currentScreenCategory.transformLatest {
        // clear previous items when category changed
        emit(emptyList())

        // map category to db items
        emitAll(
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
        )
    }.transformLatest {
        _currentUiItemsLoading.value = true
        emit(mapDbItemsToUiItems(it))
        _currentUiItemsLoading.value = false
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5.seconds.inWholeMilliseconds),
        emptyList(),
    )
}
