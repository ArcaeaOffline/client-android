package xyz.sevive.arcaeaoffline.ui.database.b30list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import xyz.sevive.arcaeaoffline.core.database.entities.Chart
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResultBest
import xyz.sevive.arcaeaoffline.helpers.ChartFactory
import xyz.sevive.arcaeaoffline.ui.containers.ArcaeaOfflineDatabaseRepositoryContainer

data class DatabaseB30ListUiItem(
    val index: Int,
    val playResultBest: PlayResultBest,
    val chart: Chart?,
) {
    val id get() = playResultBest.id
}

class DatabaseB30ListViewModel(
    private val repositoryContainer: ArcaeaOfflineDatabaseRepositoryContainer
) : ViewModel() {
    private val _limit = MutableStateFlow(INIT_LIMIT)
    val limit = _limit.asStateFlow()

    private val _b30List = MutableStateFlow<List<PlayResultBest>?>(null)
    private val b30List = _b30List.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private suspend fun updateB30ListWithLimit(limit: Int) {
        _loading.value = true

        val scores = repositoryContainer.playResultBestRepo.listDescWithLimit(limit).firstOrNull()
        _b30List.value = scores

        _loading.value = false
    }

    suspend fun setLimit(newLimit: Int) {
        _limit.value = newLimit

        updateB30ListWithLimit(newLimit)
    }

    init {
        viewModelScope.launch {
            setLimit(limit.value)
        }
    }

    val uiItems = b30List.map { scoreBests ->
        scoreBests?.mapIndexed { i, scoreBest ->
            DatabaseB30ListUiItem(
                index = i,
                playResultBest = scoreBest,
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
        const val INIT_LIMIT = 40
    }
}
