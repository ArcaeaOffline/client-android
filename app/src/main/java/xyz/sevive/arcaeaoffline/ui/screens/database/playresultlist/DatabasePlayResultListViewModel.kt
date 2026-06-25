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
import xyz.sevive.arcaeaoffline.core.database.entities.playRating
import xyz.sevive.arcaeaoffline.core.database.repositories.ChartRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.DifficultyRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.PlayResultRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.SongRepository
import xyz.sevive.arcaeaoffline.helpers.ArcaeaPlayResultValidator
import xyz.sevive.arcaeaoffline.ui.helpers.ArcaeaFormatters
import xyz.sevive.arcaeaoffline.ui.helpers.UiDisplayChartCacheHolder
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.Uuid

class DatabasePlayResultListViewModel(
    private val playResultRepo: PlayResultRepository,
    private val songRepo: SongRepository,
    private val difficultyRepo: DifficultyRepository,
    private val chartRepo: ChartRepository,
) : ViewModel() {
    enum class SortOrder {
        ASC,
        DESC,
        ;

        fun reverse() = if (this == ASC) DESC else ASC
    }

    enum class SortByValue { ID, SCORE, PLAY_RATING, DATE }

    data class ListItem(
        val playResult: PlayResult,
        val chart: Chart? = null,
        val isDeletedInGame: Boolean = false,
    ) {
        val uuid = playResult.uuid
        val playRating = chart?.let { playResult.playRating(it) }
        val potentialText =
            buildAnnotatedString {
                val baseText = ArcaeaFormatters.potentialToText(playRating)

                if (isDeletedInGame) {
                    pushStyle(
                        SpanStyle(
                            textDecoration = TextDecoration.LineThrough,
                            fontWeight = FontWeight.Light,
                        ),
                    )
                }
                append(baseText)
            }
        val warnings = ArcaeaPlayResultValidator.validate(playResult, chart)
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
    val selectedItemUuids = MutableStateFlow(emptyList<Uuid>())

    @OptIn(ExperimentalCoroutinesApi::class)
    val rawListItems =
        playResultRepo.findAll().transformLatest { dbItems ->
            isLoading.value = true

            val chartCache = UiDisplayChartCacheHolder()
            chartCache.updateCache(dbItems, songRepo, difficultyRepo, chartRepo)

            val deletedSongIds =
                songRepo
                    .findDeletedInGame()
                    .firstOrNull()
                    ?.map { it.id }
                    ?: emptyList()
            val listItems =
                dbItems.map { playResult ->
                    val chart = chartCache.get(playResult)

                    ListItem(
                        playResult = playResult,
                        chart = chart,
                        isDeletedInGame = playResult.songId in deletedSongIds,
                    )
                }

            emit(listItems)

            isLoading.value = false
        }

    private val listItems =
        combine(rawListItems, sortOrder, sortByValue) { items, order, sortBy ->
            val itemsSorted =
                when (sortBy) {
                    SortByValue.ID -> items.sortedBy { it.playResult.id }
                    SortByValue.SCORE -> items.sortedBy { it.playResult.score }
                    SortByValue.PLAY_RATING -> items.sortedBy { it.playRating }
                    SortByValue.DATE -> items.sortedBy { it.playResult.date?.toEpochMilliseconds() }
                }

            if (order == SortOrder.ASC) itemsSorted else itemsSorted.reversed()
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5.seconds.inWholeMilliseconds),
            emptyList(),
        )

    val uiState =
        combine(
            isLoading,
            listItems,
            sortOrder,
            sortByValue,
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

    fun setItemSelected(
        listItem: ListItem,
        selected: Boolean,
    ) {
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
                playResultRepo
                    .findAllByUuid(selectedItemUuids.value)
                    .firstOrNull() ?: emptyList()

            playResultRepo.deleteBatch(*playResults.toTypedArray())
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
            playResultRepo.upsert(playResult)

            if (context != null && snackbarHostState != null) {
                snackbarHostState.showSnackbar(
                    message =
                        context.getString(
                            R.string.database_play_result_updated,
                            "(${playResult.songId}, ${playResult.uuid})",
                        ),
                    withDismissAction = true,
                )
            }
        }
    }
}
