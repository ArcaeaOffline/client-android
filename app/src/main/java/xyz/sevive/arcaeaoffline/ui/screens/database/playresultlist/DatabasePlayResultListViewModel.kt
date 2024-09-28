package xyz.sevive.arcaeaoffline.ui.screens.database.playresultlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xyz.sevive.arcaeaoffline.core.database.entities.Chart
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResultWithChart
import xyz.sevive.arcaeaoffline.core.database.entities.potential
import xyz.sevive.arcaeaoffline.ui.containers.ArcaeaOfflineDatabaseRepositoryContainer
import xyz.sevive.arcaeaoffline.ui.helpers.ArcaeaFormatters


class DatabasePlayResultListViewModel(
    private val repositoryContainer: ArcaeaOfflineDatabaseRepositoryContainer
) : ViewModel() {
    data class UiItem(
        val playResult: PlayResult,
        val chart: Chart? = null,
        val potential: Double? = null,
        val potentialText: String = ArcaeaFormatters.potentialToText(potential),
        val selected: Boolean = false,
    ) {
        val id get() = playResult.id
    }

    data class UiState(
        val isInSelectMode: Boolean = false,
        val isLoading: Boolean = true,
        val uiListItems: List<UiItem> = listOf(),
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    private var _uiListItems
        get() = _uiState.value.uiListItems
        set(value) {
            _uiState.value = _uiState.value.copy(uiListItems = value)
        }

    private var _isLoading
        get() = _uiState.value.isLoading
        set(value) {
            _uiState.value = _uiState.value.copy(isLoading = value)
        }

    private var _isInSelectMode
        get() = _uiState.value.isInSelectMode
        set(value) {
            _uiState.value = _uiState.value.copy(isInSelectMode = value)
        }

    val databaseListenFlow =
        repositoryContainer.relationshipsRepo.playResultsWithCharts().transform {
            if (it == null) _isLoading = true
            else {
                _isLoading = true
                syncNewDatabaseEntries(it)
                _isLoading = false
            }

            emit(System.currentTimeMillis())
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(2500),
            initialValue = System.currentTimeMillis(),
        )

    private fun syncNewDatabaseEntries(dbEntries: List<PlayResultWithChart>) {
        val oldUiItems = _uiListItems.toList()
        val newUiItems = mutableListOf<UiItem>()

        dbEntries.forEach {
            val potential = it.chart?.let { chart -> it.playResult.potential(chart) }
            val existingUiItem = oldUiItems.find { u -> u.id == it.playResult.id }
            val uiItem = existingUiItem?.copy(
                playResult = it.playResult,
                chart = it.chart,
                potential = potential,
            ) ?: UiItem(playResult = it.playResult, chart = it.chart, potential = potential)
            newUiItems.add(uiItem)
        }

        _uiListItems = newUiItems.toList()
    }

    fun enterSelectMode() {
        _isInSelectMode = true
    }

    fun exitSelectMode() {
        clearSelectedItems()
        _isInSelectMode = false
    }

    fun setItemSelected(uiItem: UiItem, selected: Boolean) {
        val tempMutableList = _uiListItems.toMutableList()
        val idx = tempMutableList.indexOf(uiItem)
        if (idx < 0) return
        tempMutableList[idx] = uiItem.copy(selected = selected)
        _uiListItems = tempMutableList.toList()
    }

    /**
     * **DELETE** selected items **IN DATABASE**.
     *
     * @see clearSelectedItems
     */
    fun deleteSelectedItems() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val playResults = _uiListItems.filter { it.selected }.map { it.playResult }
                repositoryContainer.playResultRepo.deleteAll(*playResults.toTypedArray())
            }
        }
    }

    /**
     * Set all uiItems to unselected state.
     */
    fun clearSelectedItems() {
        _uiListItems.filter { it.selected }.forEach {
            setItemSelected(it, false)
        }
    }

    fun updateScore(playResult: PlayResult) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repositoryContainer.playResultRepo.upsert(playResult)
            }
        }
    }
}
