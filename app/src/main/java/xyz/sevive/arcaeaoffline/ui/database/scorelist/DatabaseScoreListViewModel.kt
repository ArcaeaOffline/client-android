package xyz.sevive.arcaeaoffline.ui.database.scorelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import xyz.sevive.arcaeaoffline.core.database.entities.Chart
import xyz.sevive.arcaeaoffline.core.database.entities.Score
import xyz.sevive.arcaeaoffline.core.database.entities.ScoreCalculated
import xyz.sevive.arcaeaoffline.core.helpers.ChartFactory
import xyz.sevive.arcaeaoffline.ui.containers.ArcaeaOfflineDatabaseRepositoryContainer

data class DatabaseScoreListUiItem(
    val score: Score,
    val scoreCalculated: ScoreCalculated?,
    val chart: Chart?,
) {
    val id get() = score.id
}

class DatabaseScoreListViewModel(
    private val repositoryContainer: ArcaeaOfflineDatabaseRepositoryContainer
) : ViewModel() {
    private val scoreList = repositoryContainer.scoreRepository.findAll().stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = listOf(),
    )

    private val scoreCalculatedList =
        repositoryContainer.scoreCalculatedRepository.findAll().stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = listOf(),
        )

    val uiItems = combine(scoreList, scoreCalculatedList) { scores, scoresCalculated ->
        scores.map { score ->
            DatabaseScoreListUiItem(
                score = score,
                scoreCalculated = scoresCalculated.find { it.id == score.id },
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

    fun selectItem(uiItem: DatabaseScoreListUiItem) {
        _selectedUiItemIdList.add(uiItem.id)
        _selectedUiItemIds.value = _selectedUiItemIdList.toList()
    }

    fun deselectItem(uiItem: DatabaseScoreListUiItem) {
        _selectedUiItemIdList.remove(uiItem.id)
        _selectedUiItemIds.value = _selectedUiItemIdList.toList()
    }

    suspend fun deleteSelection() {
        val items = uiItems.value.filter { _selectedUiItemIdList.contains(it.id) }
        val scores = items.map { it.score }.toTypedArray()
        repositoryContainer.scoreRepository.deleteAll(*scores)
        clearSelectedItems()
    }

    fun clearSelectedItems() {
        _selectedUiItemIdList.clear()
        _selectedUiItemIds.value = _selectedUiItemIdList.toList()
    }

    suspend fun updateScore(score: Score) {
        repositoryContainer.scoreRepository.upsert(score)
    }

    companion object {
        const val STOP_TIMEOUT_MILLIS = 7500L
    }
}
