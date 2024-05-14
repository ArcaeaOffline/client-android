package xyz.sevive.arcaeaoffline.ui.database.b30list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import xyz.sevive.arcaeaoffline.core.database.entities.Chart
import xyz.sevive.arcaeaoffline.core.database.entities.ScoreBest
import xyz.sevive.arcaeaoffline.core.helpers.ChartFactory
import xyz.sevive.arcaeaoffline.ui.containers.ArcaeaOfflineDatabaseRepositoryContainer

data class DatabaseB30ListUiItem(
    val index: Int,
    val scoreBest: ScoreBest,
    val chart: Chart?,
) {
    val id get() = scoreBest.id
}

class DatabaseB30ListViewModel(
    private val repositoryContainer: ArcaeaOfflineDatabaseRepositoryContainer
) : ViewModel() {
    private val _limit = MutableStateFlow(40)
    val limit = _limit.asStateFlow()

    fun setLimit(newLimit: Int) {
        _limit.value = newLimit
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private val b30List = limit.debounce(500L).flatMapLatest { limit ->
        repositoryContainer.scoreBestRepository.listDescWithLimit(limit).stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = listOf(),
        )
    }

    val uiItems = b30List.map { scoreBests ->
        scoreBests.mapIndexed { i, scoreBest ->
            DatabaseB30ListUiItem(
                index = i,
                scoreBest = scoreBest,
                chart = ChartFactory.getChart(
                    repositoryContainer,
                    scoreBest.songId,
                    scoreBest.ratingClass
                ),
            )
        }
    }.stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = listOf(),
    )

    companion object {
        const val STOP_TIMEOUT_MILLIS = 7500L
    }
}
