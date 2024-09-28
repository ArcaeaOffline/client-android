package xyz.sevive.arcaeaoffline.ui.screens.database.r30list

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
import org.threeten.bp.Instant
import xyz.sevive.arcaeaoffline.core.database.entities.Chart
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.core.database.entities.R30Entry
import xyz.sevive.arcaeaoffline.core.database.helpers.ChartFactory
import xyz.sevive.arcaeaoffline.jobs.R30UpdateJob
import xyz.sevive.arcaeaoffline.ui.containers.ArcaeaOfflineDatabaseRepositoryContainer
import kotlin.time.Duration.Companion.seconds


class DatabaseR30ListViewModel(
    private val workManager: WorkManager,
    private val repositoryContainer: ArcaeaOfflineDatabaseRepositoryContainer,
) : ViewModel() {
    private suspend fun getUiItemChart(playResult: PlayResult): Chart? {
        val chart = repositoryContainer.chartRepo.find(playResult).firstOrNull()
        if (chart != null) return chart

        val song = repositoryContainer.songRepo.find(playResult).firstOrNull() ?: return null
        val difficulty =
            repositoryContainer.difficultyRepo.find(playResult).firstOrNull() ?: return null
        return ChartFactory.fakeChart(song, difficulty)
    }

    data class ListItem(
        val index: Int,
        val r30Entry: R30Entry,
        val playResult: PlayResult,
        val chart: Chart?,
        val potential: Double?,
    ) {
        val id get() = playResult.id
    }

    data class UiState(
        val isLoading: Boolean = false,
        val lastUpdatedAt: Instant? = null,
        val listItems: List<ListItem> = emptyList(),
    )

    val uiState = repositoryContainer.r30EntryRepo.findAllCombined().transform { dbItems ->
        emit(UiState(isLoading = true))

        val listItems = dbItems.map { dbItem ->
            val potential = dbItem.potential()

            ListItem(
                index = -1,
                r30Entry = dbItem.entry,
                playResult = dbItem.playResult,
                chart = getUiItemChart(dbItem.playResult),
                potential = potential,
            )
        }.sortedByDescending { it.potential }.mapIndexed { i, uiItem -> uiItem.copy(index = i) }

        val lastUpdatedAt = repositoryContainer.propertyRepo.r30LastUpdatedAt()

        emit(
            UiState(isLoading = false, lastUpdatedAt = lastUpdatedAt, listItems = listItems)
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5.seconds.inWholeMilliseconds),
        UiState(),
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val updateProgress =
        workManager.getWorkInfosForUniqueWorkFlow(R30UpdateJob.WORK_NAME).mapLatest { workInfos ->
            val workInfo = workInfos.getOrNull(0) ?: return@mapLatest 0 to -1

            workInfo.progress.getInt(R30UpdateJob.KEY_PROGRESS, 0) to workInfo.progress.getInt(
                R30UpdateJob.KEY_PROGRESS_TOTAL, -1
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5.seconds.inWholeMilliseconds),
            0 to -1,
        )

    private fun enqueueWork(runMode: R30UpdateJob.RunMode) {
        val workRequest = OneTimeWorkRequestBuilder<R30UpdateJob>().setInputData(
            workDataOf(R30UpdateJob.DATA_RUN_MODE to runMode.value)
        )

        workManager.enqueueUniqueWork(
            R30UpdateJob.WORK_NAME, ExistingWorkPolicy.REPLACE, workRequest.build()
        )
    }

    fun requestUpdate() {
        enqueueWork(R30UpdateJob.RunMode.NORMAL)
    }

    fun requestRebuild() {
        enqueueWork(R30UpdateJob.RunMode.REBUILD)
    }
}
