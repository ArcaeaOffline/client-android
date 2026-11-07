package xyz.sevive.arcaeaoffline.ui.screens.ocr.queue

import android.content.Context
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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.io.files.SystemFileSystem
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.database.entities.Difficulty
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.core.database.repositories.ChartInfoRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.DatabaseReadinessRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.DifficultyWithSongRepository
import xyz.sevive.arcaeaoffline.core.validation.PlayResultValidator
import xyz.sevive.arcaeaoffline.data.OcrDependencyPaths
import xyz.sevive.arcaeaoffline.database.entities.OcrQueueTask
import xyz.sevive.arcaeaoffline.database.entities.OcrQueueTaskStatus
import xyz.sevive.arcaeaoffline.database.repositories.OcrQueueTaskRepositoryImpl
import xyz.sevive.arcaeaoffline.datastore.OcrQueuePreferencesRepository
import xyz.sevive.arcaeaoffline.datastore.OcrQueuePreferencesSerializer
import xyz.sevive.arcaeaoffline.jobs.OcrQueueProcessingJob
import xyz.sevive.arcaeaoffline.ui.helpers.UiDisplayChartCacheHolder
import xyz.sevive.arcaeaoffline.ui.helpers.UiEvent
import xyz.sevive.arcaeaoffline.ui.helpers.UiEventChannel
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class OcrQueueScreenViewModel(
    context: Context,
    private val difficultyWithSongRepo: DifficultyWithSongRepository,
    private val chartInfoRepo: ChartInfoRepository,
    databaseReadinessRepo: DatabaseReadinessRepository,
    private val ocrQueueTaskRepo: OcrQueueTaskRepositoryImpl,
    preferencesRepository: OcrQueuePreferencesRepository,
) : ViewModel() {
    companion object {
        const val LOG_TAG = "OcrQueueScreenViewModel"
    }

    private val workManager = WorkManager.getInstance(context.applicationContext)

    private val eventChannel = UiEventChannel()
    val events = eventChannel.events

    private val ocrQueuePreferences =
        preferencesRepository.preferencesFlow.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            OcrQueuePreferencesSerializer.defaultValue,
        )

    data class TaskUiItem(
        val dbItem: OcrQueueTask,
        val display: UiDisplayChartCacheHolder.Entry? = null,
    ) {
        val warnings =
            dbItem.playResult?.let { PlayResultValidator.validate(playResult = it, chartInfo = display?.chartInfo) }
        val hasWarnings = warnings?.isNotEmpty() == true

        val canEditChart = dbItem.status in listOf(OcrQueueTaskStatus.DONE, OcrQueueTaskStatus.ERROR)
        val canEditPlayResult = dbItem.status == OcrQueueTaskStatus.DONE
        val canSave = dbItem.status == OcrQueueTaskStatus.DONE && warnings != null && !hasWarnings
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

    internal val currentScreenCategory = MutableStateFlow(OcrQueueScreenCategory.NULL)

    internal fun setCurrentScreenCategory(category: OcrQueueScreenCategory) {
        currentScreenCategory.value = category
    }

    private val _isTaskUiItemsLoading = MutableStateFlow(true)
    val isTaskUiItemsLoading = _isTaskUiItemsLoading.asStateFlow()

    private suspend fun mapDbItemsToUiItems(dbItems: List<OcrQueueTask>): List<TaskUiItem> {
        val chartCacheHolder = UiDisplayChartCacheHolder()
        chartCacheHolder.updateCache(
            dbItems.mapNotNull { it.playResult }.map { it.songId to it.ratingClass },
            difficultyWithSongRepo,
            chartInfoRepo,
        )

        return dbItems.map {
            if (it.playResult == null) {
                TaskUiItem(dbItem = it)
            } else {
                TaskUiItem(dbItem = it, display = chartCacheHolder.get(it.playResult))
            }
        }
    }

    val uiItems = ocrQueueTaskRepo.findAll().mapLatest(::mapDbItemsToUiItems)

    val queueTaskCounts =
        uiItems
            .mapLatest { tasks ->
                QueueTaskCounts(
                    total = tasks.size,
                    idle = tasks.count { it.dbItem.status == OcrQueueTaskStatus.IDLE },
                    processing = tasks.count { it.dbItem.status == OcrQueueTaskStatus.PROCESSING },
                    done = tasks.count { it.dbItem.status == OcrQueueTaskStatus.DONE },
                    doneWithWarning = tasks.count { it.dbItem.status == OcrQueueTaskStatus.DONE && it.hasWarnings },
                    error = tasks.count { it.dbItem.status == OcrQueueTaskStatus.ERROR },
                )
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5.seconds.inWholeMilliseconds),
                QueueTaskCounts(),
            )

    val queueStatusUiState =
        workManager
            .getWorkInfosForUniqueWorkFlow(OcrQueueProcessingJob.WORK_NAME)
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

    val currentScreenUiItems =
        combine(currentScreenCategory, uiItems) { category, items -> category to items }
            .transformLatest { (category, items) ->
                _isTaskUiItemsLoading.value = true
                try {
                    // clear previous items when category changed
                    emit(emptyList())

                    // filter from ui items
                    val uiItemsFiltered =
                        when (category) {
                            OcrQueueScreenCategory.IDLE -> {
                                items.filter { it.dbItem.status == OcrQueueTaskStatus.IDLE }
                            }

                            OcrQueueScreenCategory.PROCESSING -> {
                                items.filter { it.dbItem.status == OcrQueueTaskStatus.PROCESSING }
                            }

                            OcrQueueScreenCategory.DONE -> {
                                items.filter { it.dbItem.status == OcrQueueTaskStatus.DONE }
                            }

                            OcrQueueScreenCategory.DONE_WITH_WARNING -> {
                                items.filter {
                                    it.dbItem.status == OcrQueueTaskStatus.DONE && it.hasWarnings
                                }
                            }

                            OcrQueueScreenCategory.ERROR -> {
                                items.filter { it.dbItem.status == OcrQueueTaskStatus.ERROR }
                            }

                            OcrQueueScreenCategory.NULL -> {
                                emptyList()
                            }
                        }

                    emit(uiItemsFiltered)
                } finally {
                    _isTaskUiItemsLoading.value = false
                }
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5.seconds.inWholeMilliseconds),
                emptyList(),
            )

    private val queueRunning =
        workManager
            .getWorkInfosForUniqueWorkFlow(OcrQueueProcessingJob.WORK_NAME)
            .map {
                it.isNotEmpty() && it[0].state == androidx.work.WorkInfo.State.RUNNING
            }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    /** Database readiness for the hint rows; null while the first load is in flight. */
    val readinessSummary =
        databaseReadinessRepo.summary().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5.seconds.inWholeMilliseconds),
            null,
        )

    fun deleteTask(taskId: Long) {
        if (queueRunning.value) return

        viewModelScope.launch { ocrQueueTaskRepo.delete(taskId) }
    }

    fun clearTasks() {
        if (queueRunning.value) return

        viewModelScope.launch { ocrQueueTaskRepo.deleteAll() }
    }

    fun modifyTaskChart(
        taskId: Long,
        difficulty: Difficulty,
    ) {
        viewModelScope.launch {
            ocrQueueTaskRepo.updateChart(taskId, difficulty)
        }
    }

    fun modifyTaskPlayResult(
        taskId: Long,
        playResult: PlayResult,
    ) {
        viewModelScope.launch {
            ocrQueueTaskRepo.updatePlayResult(taskId, playResult)
        }
    }

    fun saveTaskPlayResult(taskId: Long) {
        viewModelScope.launch {
            val task = uiItems.firstOrNull().orEmpty().firstOrNull { it.dbItem.id == taskId }
            if (task == null) return@launch
            ocrQueueTaskRepo.save(task.dbItem.id)
        }
    }

    fun saveAllTaskPlayResults() {
        if (queueRunning.value) return

        viewModelScope.launch {
            ocrQueueTaskRepo.saveBatch(
                uiItems
                    .firstOrNull()
                    .orEmpty()
                    .filter { it.canSave }
                    .map { it.dbItem },
            )
        }
    }

    fun tryStopQueue() {
        workManager.cancelUniqueWork(OcrQueueProcessingJob.WORK_NAME)
    }

    fun startQueue(runMode: OcrQueueProcessingJob.RunMode = OcrQueueProcessingJob.RunMode.NORMAL) {
        viewModelScope.launch {
            // NORMAL and ALL needs the hashes database before processing any task
            if (runMode != OcrQueueProcessingJob.RunMode.SMART_FIX && !imageHashesDatabaseFileExists()) {
                eventChannel.send(UiEvent.StringRes(R.string.ocr_queue_event_ihdb_missing))
                return@launch
            }

            val workRequest =
                OneTimeWorkRequestBuilder<OcrQueueProcessingJob>()
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .setInputData(
                        workDataOf(
                            OcrQueueProcessingJob.DATA_RUN_MODE to runMode.value,
                            OcrQueueProcessingJob.DATA_PARALLEL_COUNT to ocrQueuePreferences.value.parallelCount,
                        ),
                    ).build()

            workManager.enqueueUniqueWork(
                OcrQueueProcessingJob.WORK_NAME,
                ExistingWorkPolicy.KEEP,
                workRequest,
            )
        }
    }

    private suspend fun imageHashesDatabaseFileExists(): Boolean =
        withContext(Dispatchers.IO) {
            SystemFileSystem.exists(OcrDependencyPaths().imageHashesDatabaseFile)
        }
}
