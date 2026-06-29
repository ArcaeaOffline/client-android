package xyz.sevive.arcaeaoffline.ui.screens.database.r30list

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform
import xyz.sevive.arcaeaoffline.core.Progress
import xyz.sevive.arcaeaoffline.core.database.entities.Chart
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.core.database.entities.R30Entry
import xyz.sevive.arcaeaoffline.core.database.helpers.ChartFactory
import xyz.sevive.arcaeaoffline.core.database.repositories.ChartRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.DifficultyRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.PropertyRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.R30EntryRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.SongRepository
import xyz.sevive.arcaeaoffline.helpers.fromWorkInfo
import xyz.sevive.arcaeaoffline.jobs.R30UpdateJob
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class DatabaseR30ListViewModel(
    context: Context,
    r30EntryRepo: R30EntryRepository,
    private val propertyRepo: PropertyRepository,
    private val chartRepo: ChartRepository,
    private val songRepo: SongRepository,
    private val difficultyRepo: DifficultyRepository,
) : ViewModel() {
    private val workManager = WorkManager.getInstance(context.applicationContext)

    private suspend fun getUiItemChart(playResult: PlayResult): Chart? {
        val chart = chartRepo.find(playResult).firstOrNull()
        if (chart != null) return chart

        val song = songRepo.find(playResult).firstOrNull() ?: return null
        val difficulty =
            difficultyRepo.find(playResult).firstOrNull() ?: return null
        return ChartFactory.fakeChart(song, difficulty)
    }

    data class ListItem(
        val index: Int,
        val r30Entry: R30Entry,
        val playResult: PlayResult,
        val chart: Chart?,
        val playRating: Double?,
    ) {
        val id get() = playResult.id
    }

    data class UiState(
        val isLoading: Boolean = false,
        val lastUpdatedAt: Instant? = null,
        val listItems: List<ListItem> = emptyList(),
    )

    val uiState =
        r30EntryRepo
            .findAllCombined()
            .transform { dbItems ->
                emit(UiState(isLoading = true))

                val listItems =
                    dbItems
                        .map { dbItem ->
                            ListItem(
                                index = -1,
                                r30Entry = dbItem.entry,
                                playResult = dbItem.playResult,
                                chart = getUiItemChart(dbItem.playResult),
                                playRating = dbItem.playRating(),
                            )
                        }.sortedByDescending { it.playRating }
                        .mapIndexed { i, uiItem -> uiItem.copy(index = i) }

                val lastUpdatedAt = propertyRepo.r30LastUpdatedAt()

                emit(
                    UiState(isLoading = false, lastUpdatedAt = lastUpdatedAt, listItems = listItems),
                )
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5.seconds.inWholeMilliseconds),
                UiState(),
            )

    @OptIn(ExperimentalCoroutinesApi::class)
    val updateProgress =
        workManager
            .getWorkInfosForUniqueWorkFlow(R30UpdateJob.WORK_NAME)
            .mapLatest { workInfos -> Progress.fromWorkInfo(workInfos.getOrNull(0)) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5.seconds.inWholeMilliseconds),
                null,
            )

    private fun enqueueWork(runMode: R30UpdateJob.RunMode) {
        val workRequest =
            OneTimeWorkRequestBuilder<R30UpdateJob>().setInputData(
                workDataOf(R30UpdateJob.DATA_RUN_MODE to runMode.value),
            )

        workManager.enqueueUniqueWork(
            R30UpdateJob.WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            workRequest.build(),
        )
    }

    fun requestUpdate() {
        enqueueWork(R30UpdateJob.RunMode.NORMAL)
    }

    fun requestRebuild() {
        enqueueWork(R30UpdateJob.RunMode.REBUILD)
    }
}
