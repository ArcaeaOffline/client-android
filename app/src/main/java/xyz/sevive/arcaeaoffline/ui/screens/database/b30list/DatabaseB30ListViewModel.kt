package xyz.sevive.arcaeaoffline.ui.screens.database.b30list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import xyz.sevive.arcaeaoffline.core.database.entities.Chart
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResultBest
import xyz.sevive.arcaeaoffline.ui.containers.ArcaeaOfflineDatabaseRepositoryContainer
import xyz.sevive.arcaeaoffline.ui.helpers.ArcaeaFormatters
import kotlin.time.Duration.Companion.seconds


class DatabaseB30ListViewModel(
    private val repositoryContainer: ArcaeaOfflineDatabaseRepositoryContainer
) : ViewModel() {
    data class ListItem(
        val index: Int,
        val playResultBest: PlayResultBest,
        val chart: Chart?,
        val potentialText: String = ArcaeaFormatters.potentialToText(playResultBest.potential),
    )

    data class UiState(
        val isLoading: Boolean = false,
        val limit: Int = 0,
        val listItems: List<ListItem> = emptyList(),
    )

    private val limit = MutableStateFlow(INIT_LIMIT)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState = limit.transformLatest { limit ->
        emit(UiState(isLoading = true, limit = limit))

        repositoryContainer.relationshipsRepo.playResultsBestWithCharts(limit)
            .collectLatest { dbItems ->
                val listItems = dbItems.mapIndexed { i, dbItem ->
                    ListItem(
                        index = i, playResultBest = dbItem.playResultBest, chart = dbItem.chart
                    )
                }

                emit(UiState(isLoading = false, limit = limit, listItems = listItems))
            }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5.seconds.inWholeMilliseconds),
        UiState(),
    )

    fun setLimit(limit: Int) {
        this.limit.value = limit
    }

    fun forceReload() {
        val limitValue = this.limit.value
        this.limit.value = 0
        this.limit.value = limitValue
    }

    companion object {
        const val INIT_LIMIT = 40
    }
}
