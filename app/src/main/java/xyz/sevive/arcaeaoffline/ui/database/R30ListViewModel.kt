package xyz.sevive.arcaeaoffline.ui.database

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.threeten.bp.Instant
import xyz.sevive.arcaeaoffline.core.database.entities.Chart
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.core.database.entities.R30Entry
import xyz.sevive.arcaeaoffline.core.database.helpers.ChartFactory
import xyz.sevive.arcaeaoffline.helpers.formatAsLocalizedDateTime
import xyz.sevive.arcaeaoffline.ui.containers.ArcaeaOfflineDatabaseRepositoryContainer
import xyz.sevive.arcaeaoffline.ui.helpers.ArcaeaFormatters


class DatabaseR30ListViewModel(
    private val repositoryContainer: ArcaeaOfflineDatabaseRepositoryContainer,
) : ViewModel() {
    private val r30EntryRepo = repositoryContainer.r30EntryRepo

    private suspend fun getUiItemChart(playResult: PlayResult): Chart? {
        val chart = repositoryContainer.chartRepo.find(playResult).firstOrNull()
        if (chart != null) return chart

        val song = repositoryContainer.songRepo.find(playResult).firstOrNull() ?: return null
        val difficulty =
            repositoryContainer.difficultyRepo.find(playResult).firstOrNull() ?: return null
        return ChartFactory.fakeChart(song, difficulty)
    }

    data class UiItem(
        val index: Int,
        val r30Entry: R30Entry,
        val playResult: PlayResult,
        val chart: Chart?,
        val potential: Double?,
        val potentialText: String,
    ) {
        val id get() = playResult.id
    }

    data class UiState(
        val isLoading: Boolean = false,
        val lastUpdatedAt: Instant? = null,
        val lastUpdatedAtText: String = "-",
        val uiItems: List<UiItem> = emptyList(),
    )

    val uiState = r30EntryRepo.updating.transform {
        emit(UiState(isLoading = true))

        // if the repository is updating, keep the loading state showing in UI
        if (it) return@transform

        val dbItems = r30EntryRepo.findAllCombined().firstOrNull() ?: emptyList()
        val uiItems = dbItems.map { dbItem ->
            val potential = dbItem.potential()

            UiItem(
                index = -1,
                r30Entry = dbItem.entry,
                playResult = dbItem.playResult,
                chart = getUiItemChart(dbItem.playResult),
                potential = potential,
                potentialText = ArcaeaFormatters.potentialToText(potential),
            )
        }.sortedByDescending { it.potential }.mapIndexed { i, uiItem -> uiItem.copy(index = i) }

        val lastUpdatedAt = repositoryContainer.propertyRepo.r30LastUpdatedAt()
        val lastUpdatedAtText = lastUpdatedAt?.formatAsLocalizedDateTime() ?: "-"

        emit(
            UiState(
                isLoading = false,
                lastUpdatedAt = lastUpdatedAt,
                lastUpdatedAtText = lastUpdatedAtText,
                uiItems = uiItems
            )
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState())

    val updateProgress = r30EntryRepo.updateProgress.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        0 to -1,
    )

    fun requestUpdate() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { r30EntryRepo.requestUpdate() }
        }
    }

    fun requestRebuild() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { r30EntryRepo.requestRebuild() }
        }
    }
}
