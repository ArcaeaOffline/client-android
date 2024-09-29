package xyz.sevive.arcaeaoffline.ui.screens.database.playresultlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import xyz.sevive.arcaeaoffline.core.database.entities.Chart
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.core.database.entities.potential
import xyz.sevive.arcaeaoffline.ui.containers.ArcaeaOfflineDatabaseRepositoryContainer
import xyz.sevive.arcaeaoffline.ui.helpers.ArcaeaFormatters
import java.util.UUID
import kotlin.time.Duration.Companion.seconds


class DatabasePlayResultListViewModel(
    private val repositoryContainer: ArcaeaOfflineDatabaseRepositoryContainer
) : ViewModel() {
    data class ListItem(
        val playResult: PlayResult,
        val chart: Chart? = null,
        val potential: Double? = null,
        val potentialText: String = ArcaeaFormatters.potentialToText(potential),
    ) {
        val uuid = playResult.uuid
    }

    data class UiState(
        val isLoading: Boolean = true,
        val listItems: List<ListItem> = emptyList(),
    )

    val selectedItemUuids = MutableStateFlow(emptyList<UUID>())

    val uiState =
        repositoryContainer.relationshipsRepo.playResultsWithCharts().transform { dbItems ->
            emit(UiState(isLoading = true))

            val listItems = dbItems?.map {
                ListItem(
                    playResult = it.playResult,
                    chart = it.chart,
                    potential = it.chart?.let { chart -> it.playResult.potential(chart) },
                )
            } ?: emptyList()

            emit(UiState(isLoading = false, listItems = listItems))
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5.seconds.inWholeMilliseconds),
            initialValue = UiState(),
        )

    fun setItemSelected(listItem: ListItem, selected: Boolean) {
        if (selected) {
            selectedItemUuids.value += listItem.playResult.uuid
        } else {
            selectedItemUuids.value -= listItem.playResult.uuid
        }
    }

    /**
     * **DELETE** selected items **IN DATABASE**.
     */
    fun deleteSelectedItemsInDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            val playResults =
                repositoryContainer.playResultRepo.findAllByUUID(selectedItemUuids.value)
                    .firstOrNull() ?: emptyList()

            repositoryContainer.playResultRepo.deleteBatch(*playResults.toTypedArray())
            clearSelectedItems()
        }
    }

    /**
     * Set all uiItems to unselected state.
     */
    fun clearSelectedItems() {
        selectedItemUuids.value = emptyList()
    }

    fun updatePlayResult(playResult: PlayResult) {
        viewModelScope.launch(Dispatchers.IO) {
            repositoryContainer.playResultRepo.upsert(playResult)
        }
    }
}
