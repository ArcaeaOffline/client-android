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
import org.threeten.bp.format.DateTimeFormatter
import xyz.sevive.arcaeaoffline.core.database.entities.Chart
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.core.database.entities.R30Entry
import xyz.sevive.arcaeaoffline.core.database.entities.potential
import xyz.sevive.arcaeaoffline.ui.containers.ArcaeaOfflineDatabaseRepositoryContainer
import xyz.sevive.arcaeaoffline.ui.helpers.ArcaeaFormatters


class DatabaseR30ListViewModel(
    private val repositoryContainer: ArcaeaOfflineDatabaseRepositoryContainer,
    private val dateTimeFormatter: DateTimeFormatter,
) : ViewModel() {
    private val r30EntryRepo = repositoryContainer.r30EntryRepo

    data class UiItem(
        val index: Int,
        val r30Entry: R30Entry,
        val playResult: PlayResult,
        val chart: Chart?,
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

        val dbItems = r30EntryRepo.findAll().firstOrNull() ?: emptyList()
        val uiItems = dbItems.mapIndexed { i, dbItem ->
            val chart = repositoryContainer.chartRepo.find(dbItem.playResult).firstOrNull()
            val potential = if (chart != null) dbItem.playResult.potential(chart) else null

            UiItem(
                index = i,
                r30Entry = dbItem.r30Entry,
                playResult = dbItem.playResult,
                chart = chart ?: repositoryContainer.chartRepo.find(dbItem.playResult)
                    .firstOrNull(),
                potentialText = ArcaeaFormatters.potentialToText(potential),
            )
        }

        val lastUpdatedAt = repositoryContainer.propertyRepo.r30LastUpdatedAt()
        val lastUpdatedAtText =
            if (lastUpdatedAt != null) dateTimeFormatter.format(lastUpdatedAt) else "-"

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
