package xyz.sevive.arcaeaoffline.ui.screens.ocr.queue

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.workDataOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import org.koin.core.annotation.Provided
import xyz.sevive.arcaeaoffline.core.database.entities.Chart
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.core.database.repositories.ChartInfoRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.ChartRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.DifficultyRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.PlayResultRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.SongRepository
import xyz.sevive.arcaeaoffline.core.ocr.device.DeviceOcrResult
import xyz.sevive.arcaeaoffline.database.entities.OcrQueueTask
import xyz.sevive.arcaeaoffline.database.entities.OcrQueueTaskStatus
import xyz.sevive.arcaeaoffline.database.repositories.OcrQueueTaskRepositoryImpl
import xyz.sevive.arcaeaoffline.datastore.OcrQueuePreferencesRepository
import xyz.sevive.arcaeaoffline.datastore.OcrQueuePreferencesSerializer
import xyz.sevive.arcaeaoffline.helpers.ArcaeaPlayResultValidatorWarning
import xyz.sevive.arcaeaoffline.jobs.OcrQueueJob
import xyz.sevive.arcaeaoffline.ui.helpers.UiDisplayChartCacheHolder
import kotlin.time.Duration.Companion.seconds

class OcrQueueScreenViewModel(
    // TODO: evaluate this usage
    @Provided private val workManager: WorkManager,
    private val chartInfoRepo: ChartInfoRepository,
    private val playResultRepo: PlayResultRepository,
    private val songRepo: SongRepository,
    private val difficultyRepo: DifficultyRepository,
    private val chartRepo: ChartRepository,
    private val ocrQueueTaskRepo: OcrQueueTaskRepositoryImpl,
    private val preferencesRepository: OcrQueuePreferencesRepository,
) : ViewModel() {
    companion object {
        const val LOG_TAG = "OcrQueueScreenViewModel"
    }

    private val ocrQueuePreferences =
        preferencesRepository.preferencesFlow.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            OcrQueuePreferencesSerializer.defaultValue,
        )

    data class TaskUiItem(
        val id: Long,
        val fileUri: Uri,
        val status: OcrQueueTaskStatus,
        val ocrResult: DeviceOcrResult? = null,
        val playResult: PlayResult? = null,
        val chart: Chart? = null,
        val exception: Exception? = null,
        val warnings: List<ArcaeaPlayResultValidatorWarning> = emptyList(),
    ) {
        val canEditChart = status in listOf(OcrQueueTaskStatus.DONE, OcrQueueTaskStatus.ERROR)
        val canEditPlayResult = status == OcrQueueTaskStatus.DONE

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
            warnings = dbItem.warnings ?: emptyList(),
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

    val queueTaskCounts =
        combine(
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
    val queueStatusUiState =
        workManager
            .getWorkInfosForUniqueWorkFlow(OcrQueueJob.WORK_NAME)
            .map {
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
        workManager
            .getWorkInfosForUniqueWorkFlow(OcrQueueJob.WORK_NAME)
            .map {
                it.isNotEmpty() && it[0].state == androidx.work.WorkInfo.State.RUNNING
            }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun deleteTask(taskId: Long) {
        if (queueRunning.value) return

        viewModelScope.launch(Dispatchers.IO) { ocrQueueTaskRepo.delete(taskId) }
    }

    fun clearTasks() {
        if (queueRunning.value) return

        viewModelScope.launch(Dispatchers.IO) { ocrQueueTaskRepo.deleteAll() }
    }

    fun modifyTaskChart(
        taskId: Long,
        chart: Chart,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            ocrQueueTaskRepo.updateChart(taskId, chart)
        }
    }

    fun modifyTaskPlayResult(
        taskId: Long,
        playResult: PlayResult,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            ocrQueueTaskRepo.updatePlayResult(taskId, playResult, chartInfoRepo)
        }
    }

    fun saveTaskPlayResult(taskId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            ocrQueueTaskRepo.save(taskId, playResultRepo)
        }
    }

    fun saveAllTaskPlayResults() {
        if (queueRunning.value) return

        viewModelScope.launch(Dispatchers.IO) {
            ocrQueueTaskRepo.saveAll(playResultRepo)
        }
    }

    fun tryStopQueue() {
        workManager.cancelUniqueWork(OcrQueueJob.WORK_NAME)
    }

    fun startQueue(runMode: OcrQueueJob.RunMode = OcrQueueJob.RunMode.NORMAL) {
        viewModelScope.launch(Dispatchers.Default) {
            val workRequest =
                OneTimeWorkRequestBuilder<OcrQueueJob>()
                    .setExpedited(OutOfQuotaPolicy.DROP_WORK_REQUEST)
                    .setInputData(
                        workDataOf(
                            OcrQueueJob.DATA_RUN_MODE to runMode.value,
                            OcrQueueJob.DATA_PARALLEL_COUNT to ocrQueuePreferences.value.parallelCount,
                        ),
                    ).build()

            workManager.enqueueUniqueWork(
                OcrQueueJob.WORK_NAME,
                ExistingWorkPolicy.KEEP,
                workRequest,
            )
        }
    }

    internal val currentScreenCategory = MutableStateFlow(OcrQueueScreenCategory.NULL)

    internal fun setCurrentScreenCategory(category: OcrQueueScreenCategory) {
        currentScreenCategory.value = category
    }

    private val _isTaskUiItemsLoading = MutableStateFlow(true)
    val isTaskUiItemsLoading = _isTaskUiItemsLoading.asStateFlow()

    private suspend fun mapDbItemsToUiItems(dbItems: List<OcrQueueTask>): List<TaskUiItem> {
        val chartCacheHolder = UiDisplayChartCacheHolder()
        chartCacheHolder.updateCache(dbItems.mapNotNull { it.playResult }, songRepo, difficultyRepo, chartRepo)

        return dbItems.map {
            if (it.playResult == null) {
                TaskUiItem(dbItem = it)
            } else {
                TaskUiItem(dbItem = it, chart = chartCacheHolder.get(it.playResult))
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val taskUiItems =
        currentScreenCategory
            .transformLatest {
                // clear previous items when category changed
                emit(emptyList())

                // map category to db items
                emitAll(
                    when (it) {
                        OcrQueueScreenCategory.NULL -> {
                            emptyFlow()
                        }

                        OcrQueueScreenCategory.DONE_WITH_WARNING -> {
                            ocrQueueTaskRepo.findDoneWithWarning()
                        }

                        else -> {
                            val taskStatus =
                                when (it) {
                                    OcrQueueScreenCategory.IDLE -> OcrQueueTaskStatus.IDLE
                                    OcrQueueScreenCategory.PROCESSING -> OcrQueueTaskStatus.PROCESSING
                                    OcrQueueScreenCategory.DONE -> OcrQueueTaskStatus.DONE
                                    OcrQueueScreenCategory.ERROR -> OcrQueueTaskStatus.ERROR
                                }
                            ocrQueueTaskRepo.findByStatus(taskStatus)
                        }
                    },
                )
            }.transformLatest {
                _isTaskUiItemsLoading.value = true
                emit(mapDbItemsToUiItems(it))
                _isTaskUiItemsLoading.value = false
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5.seconds.inWholeMilliseconds),
                emptyList(),
            )
}
