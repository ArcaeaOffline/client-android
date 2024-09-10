package xyz.sevive.arcaeaoffline.ui.screens.database.b30list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform
import xyz.sevive.arcaeaoffline.core.database.entities.Chart
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResultBest
import xyz.sevive.arcaeaoffline.ui.containers.ArcaeaOfflineDatabaseRepositoryContainer
import xyz.sevive.arcaeaoffline.ui.helpers.ArcaeaFormatters


class DatabaseB30ListViewModel(
    private val repositoryContainer: ArcaeaOfflineDatabaseRepositoryContainer
) : ViewModel() {
    data class UiItem(
        val index: Int,
        val playResultBest: PlayResultBest,
        val chart: Chart?,
        val potentialText: String = ArcaeaFormatters.potentialToText(playResultBest.potential),
    ) {
        val id get() = playResultBest.id
    }

    data class UiState(
        val isLoading: Boolean = false,
        val uiItems: List<UiItem> = emptyList(),
    )

    private val _limit = MutableStateFlow(INIT_LIMIT)
    val limit = _limit.asStateFlow()

    val uiState = _limit.transform {
        emit(UiState(isLoading = true))

        val dbItems = repositoryContainer.relationshipsRepo.playResultsBestWithCharts(it)
            .firstOrNull() ?: emptyList()
        val uiItems = dbItems.mapIndexed { i, dbItem ->
            UiItem(index = i, playResultBest = dbItem.playResultBest, chart = dbItem.chart)
        }

        emit(UiState(isLoading = false, uiItems = uiItems))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState())

    fun setLimit(limit: Int) {
        _limit.value = limit
    }

    companion object {
        const val INIT_LIMIT = 40
    }
}
