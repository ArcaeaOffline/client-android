package xyz.sevive.arcaeaoffline.ui.screens.database.playresultlist

import android.content.Context
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import xyz.sevive.arcaeaoffline.R
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
    enum class SortOrder {
        ASC, DESC;

        fun reverse() = if (this == ASC) DESC else ASC
    }

    enum class SortByValue { ID, SCORE, POTENTIAL, DATE }

    data class ListItem(
        val playResult: PlayResult,
        val chart: Chart? = null,
        val potential: Double? = null,
        val isDeletedInGame: Boolean = false,
    ) {
        val uuid = playResult.uuid
        val potentialText = buildAnnotatedString {
            val baseText = ArcaeaFormatters.potentialToText(potential)

            if (isDeletedInGame) pushStyle(
                SpanStyle(
                    textDecoration = TextDecoration.LineThrough,
                    fontWeight = FontWeight.Light,
                )
            )
            append(baseText)
        }
    }

    data class UiState(
        val isLoading: Boolean = true,
        val sortOrder: SortOrder = SortOrder.ASC,
        val sortByValue: SortByValue = SortByValue.ID,
        val listItems: List<ListItem> = emptyList(),
    )

    private val isLoading = MutableStateFlow(false)
    private val sortOrder = MutableStateFlow(SortOrder.ASC)
    private val sortByValue = MutableStateFlow(SortByValue.ID)
    val selectedItemUuids = MutableStateFlow(emptyList<UUID>())

    @OptIn(ExperimentalCoroutinesApi::class)
    val rawListItems =
        repositoryContainer.relationshipsRepo.playResultsWithCharts().transformLatest { dbItems ->
            isLoading.value = true

            kotlinx.coroutines.delay(2000L)

            val deletedSongIds =
                repositoryContainer.songRepo.findDeletedInGame().firstOrNull()?.map { it.id }
                    ?: emptyList()
            val listItems = dbItems?.map {
                ListItem(
                    playResult = it.playResult,
                    chart = it.chart,
                    potential = it.chart?.let { chart -> it.playResult.potential(chart) },
                    isDeletedInGame = it.playResult.songId in deletedSongIds,
                )
            } ?: emptyList()

            emit(listItems)

            isLoading.value = false
        }

    private val listItems = combine(rawListItems, sortOrder, sortByValue) { items, order, sortBy ->
        val itemsSorted = when (sortBy) {
            SortByValue.ID -> items.sortedBy { it.playResult.id }
            SortByValue.SCORE -> items.sortedBy { it.playResult.score }
            SortByValue.POTENTIAL -> items.sortedBy { it.potential }
            SortByValue.DATE -> items.sortedBy { it.playResult.date?.toEpochMilli() }
        }

        if (order == SortOrder.ASC) itemsSorted else itemsSorted.reversed()
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5.seconds.inWholeMilliseconds),
        emptyList(),
    )

    val uiState = combine(
        isLoading, listItems, sortOrder, sortByValue
    ) { isLoading, listItems, sortOrder, sortByValue ->
        UiState(
            isLoading = isLoading,
            listItems = listItems,
            sortOrder = sortOrder,
            sortByValue = sortByValue,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5.seconds.inWholeMilliseconds),
        initialValue = UiState(),
    )

    fun setSortOrder(value: SortOrder) {
        sortOrder.value = value
    }

    fun setSortByValue(value: SortByValue) {
        sortByValue.value = value
    }

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

    fun updatePlayResult(
        playResult: PlayResult,
        context: Context? = null,
        snackbarHostState: SnackbarHostState? = null,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repositoryContainer.playResultRepo.upsert(playResult)

            if (context != null && snackbarHostState != null) {
                snackbarHostState.showSnackbar(
                    message = context.getString(
                        R.string.database_play_result_updated,
                        "(${playResult.songId}, ${playResult.uuid})"
                    ),
                    withDismissAction = true,
                )
            }
        }
    }
}
