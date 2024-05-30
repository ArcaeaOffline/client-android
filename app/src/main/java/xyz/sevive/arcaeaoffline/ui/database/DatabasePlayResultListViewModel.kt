package xyz.sevive.arcaeaoffline.ui.database

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import xyz.sevive.arcaeaoffline.core.database.entities.Chart
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResultCalculated
import xyz.sevive.arcaeaoffline.helpers.ChartFactory
import xyz.sevive.arcaeaoffline.ui.containers.ArcaeaOfflineDatabaseRepositoryContainer

data class DatabasePlayResultListUiItem(
    val playResult: PlayResult,
    val playResultCalculated: PlayResultCalculated?,
    val chart: Chart?,
) {
    val id get() = playResult.id
}

class DatabasePlayResultListViewModel(
    private val repositoryContainer: ArcaeaOfflineDatabaseRepositoryContainer
) : ViewModel() {
    private val scoreList = repositoryContainer.playResultRepo.findAll().stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = listOf(),
    )

    private val scoreCalculatedList =
        repositoryContainer.playResultCalculatedRepo.findAll().stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = listOf(),
        )

    val uiItems = combine(scoreList, scoreCalculatedList) { scores, scoresCalculated ->
        scores.map { score ->
            DatabasePlayResultListUiItem(
                playResult = score,
                playResultCalculated = scoresCalculated.find { it.id == score.id },
                chart = ChartFactory.getChart(repositoryContainer, score.songId, score.ratingClass),
            )
        }
    }.stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = listOf(),
    )

    private val _selectedUiItemIdList = mutableListOf<Int>()
    private val _selectedUiItemIds = MutableStateFlow(listOf<Int>())
    val selectedUiItemIds = _selectedUiItemIds.asStateFlow()

    fun selectItem(uiItem: DatabasePlayResultListUiItem) {
        _selectedUiItemIdList.add(uiItem.id)
        _selectedUiItemIds.value = _selectedUiItemIdList.toList()
    }

    fun deselectItem(uiItem: DatabasePlayResultListUiItem) {
        _selectedUiItemIdList.remove(uiItem.id)
        _selectedUiItemIds.value = _selectedUiItemIdList.toList()
    }

    suspend fun deleteSelection() {
        val items = uiItems.value.filter { _selectedUiItemIdList.contains(it.id) }
        val scores = items.map { it.playResult }.toTypedArray()
        repositoryContainer.playResultRepo.deleteAll(*scores)
        clearSelectedItems()
    }

    fun clearSelectedItems() {
        _selectedUiItemIdList.clear()
        _selectedUiItemIds.value = _selectedUiItemIdList.toList()
    }

    suspend fun updateScore(playResult: PlayResult) {
        repositoryContainer.playResultRepo.upsert(playResult)
    }

    companion object {
        const val STOP_TIMEOUT_MILLIS = 7500L
    }
}
