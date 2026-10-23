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
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaScoringMode
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.core.database.entities.playRating
import xyz.sevive.arcaeaoffline.core.database.repositories.ChartInfoRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.DifficultyWithSongRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.PlayResultRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.PropertyRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.SongRepository
import xyz.sevive.arcaeaoffline.core.search.ArcaeaCatalog
import xyz.sevive.arcaeaoffline.core.search.ArcaeaCatalogStateHolder
import xyz.sevive.arcaeaoffline.core.search.SearchEngine
import xyz.sevive.arcaeaoffline.helpers.ArcaeaPlayResultValidator
import xyz.sevive.arcaeaoffline.ui.helpers.ArcaeaFormatters
import xyz.sevive.arcaeaoffline.ui.helpers.UiDisplayChartCacheHolder
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.Uuid

class DatabasePlayResultListViewModel(
    private val playResultRepo: PlayResultRepository,
    private val songRepo: SongRepository,
    private val difficultyWithSongRepo: DifficultyWithSongRepository,
    private val chartInfoRepo: ChartInfoRepository,
    propertyRepo: PropertyRepository,
    catalogHolder: ArcaeaCatalogStateHolder,
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
        val display: UiDisplayChartCacheHolder.Entry? = null,
        val isDeletedInGame: Boolean = false,
        val scoringMode: ArcaeaScoringMode,
    ) {
        val uuid = playResult.uuid
        val playRating = display?.chartInfo?.let { playResult.playRating(it, scoringMode) }
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
        val warnings = ArcaeaPlayResultValidator.validate(playResult, display?.chartInfo, scoringMode)
    }

    data class UiState(
        val isLoading: Boolean = true,
        val sortOrder: SortOrder = SortOrder.DESC,
        val sortByValue: SortByValue = SortByValue.DATE,
        val filter: PlayResultFilter = PlayResultFilter(),
        val totalCount: Int = 0,
        val listItems: List<ListItem> = emptyList(),
        val songPlayResultCounts: Map<String, Int> = emptyMap(),
        val ratingClassCounts: Map<ArcaeaRatingClass, Int> = emptyMap(),
    )

    private val isLoading = MutableStateFlow(true)
    private val sortOrder = MutableStateFlow(SortOrder.DESC)
    private val sortByValue = MutableStateFlow(SortByValue.DATE)
    val selectedItemUuids = MutableStateFlow(emptyList<Uuid>())

    private val filter = MutableStateFlow(PlayResultFilter())

    val catalog: StateFlow<ArcaeaCatalog> = catalogHolder.catalog

    @OptIn(ExperimentalCoroutinesApi::class)
    private val rawListItems =
        combine(playResultRepo.findAll(), propertyRepo.scoringMode()) { dbItems, scoringMode ->
            dbItems to scoringMode
        }.transformLatest { (dbItems, scoringMode) ->
            isLoading.value = true

            val chartCache = UiDisplayChartCacheHolder()
            chartCache.updateCache(
                dbItems.map { it.songId to it.ratingClass },
                difficultyWithSongRepo,
                chartInfoRepo,
            )

            val deletedSongIds =
                songRepo
                    .findDeletedInGame()
                    .firstOrNull()
                    ?.map { it.id }
                    ?: emptyList()
            val listItems =
                dbItems.map { playResult ->
                    val display = chartCache.get(playResult)

                    ListItem(
                        playResult = playResult,
                        display = display,
                        isDeletedInGame = playResult.songId in deletedSongIds,
                        scoringMode = scoringMode,
                    )
                }

            emit(listItems)

            isLoading.value = false
        }.flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT.inWholeMilliseconds), emptyList())

    /**
     * The text query resolved to matching song ids.
     *
     * [songIds] is null when the query does not constrain the list: empty query,
     * catalog not loaded yet, or no song data imported (the list then falls back
     * to matching the raw song id).
     */
    private data class SongSearchState(
        val query: String,
        val songIds: Set<String>?,
    )

    @OptIn(FlowPreview::class)
    private val songSearchState: StateFlow<SongSearchState> =
        combine(
            filter.map { it.query }.distinctUntilChanged().debounce(SEARCH_QUERY_DEBOUNCE),
            catalog,
        ) { query, catalog ->
            when {
                query.isBlank() -> SongSearchState(query, null)
                !catalog.isLoaded -> SongSearchState(query, null)
                catalog.songs.isEmpty() -> SongSearchState(query, null)
                else -> SongSearchState(query, SearchEngine.searchIds(catalog.documents, query))
            }
        }.flowOn(Dispatchers.Default)
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(STOP_TIMEOUT.inWholeMilliseconds),
                SongSearchState(query = "", songIds = null),
            )

    private val filteredListItems: StateFlow<List<ListItem>> =
        combine(rawListItems, filter, songSearchState, catalog) { items, filter, search, catalog ->
            val allowedSongIds = filter.allowedSongIds(catalog, search.songIds)
            items.filter { it.matches(allowedSongIds, search.query, catalog, filter.onlyWarnings, filter.ratingClasses) }
        }.flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT.inWholeMilliseconds), emptyList())

    /**
     * Play results per difficulty class, counted under the other active facets.
     *
     * The difficulty selection is ignored so a chip keeps showing what selecting
     * it would yield, and the chips do not change while the selection changes.
     */
    private val ratingClassCounts: StateFlow<Map<ArcaeaRatingClass, Int>> =
        combine(
            rawListItems,
            filter.map { it.copy(ratingClasses = emptySet()) }.distinctUntilChanged(),
            songSearchState,
            catalog,
        ) { items, filter, search, catalog ->
            val allowedSongIds = filter.allowedSongIds(catalog, search.songIds)
            val counts = HashMap<ArcaeaRatingClass, Int>(ArcaeaRatingClass.entries.size)

            items.forEach { item ->
                if (item.matches(allowedSongIds, search.query, catalog, filter.onlyWarnings, emptySet())) {
                    counts.merge(item.playResult.ratingClass, 1, Int::plus)
                }
            }

            counts
        }.flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT.inWholeMilliseconds), emptyMap())

    private val listItems: StateFlow<List<ListItem>> =
        combine(filteredListItems, sortOrder, sortByValue) { items, order, sortBy ->
            val itemsSorted =
                when (sortBy) {
                    SortByValue.ID -> items.sortedBy { it.playResult.id }
                    SortByValue.SCORE -> items.sortedBy { it.playResult.score }
                    SortByValue.PLAY_RATING -> items.sortedBy { it.playRating }
                    SortByValue.DATE -> items.sortedBy { it.playResult.date?.toEpochMilliseconds() }
                }

            if (order == SortOrder.ASC) itemsSorted else itemsSorted.reversed()
        }.flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT.inWholeMilliseconds), emptyList())

    private data class ListSnapshot(
        val listItems: List<ListItem>,
        val totalCount: Int,
        val songPlayResultCounts: Map<String, Int>,
        val ratingClassCounts: Map<ArcaeaRatingClass, Int>,
    )

    private val listSnapshot: StateFlow<ListSnapshot> =
        combine(listItems, rawListItems, ratingClassCounts) { items, rawItems, ratingClassCounts ->
            val songPlayResultCounts = HashMap<String, Int>(rawItems.size)
            rawItems.forEach { songPlayResultCounts.merge(it.playResult.songId, 1, Int::plus) }

            ListSnapshot(
                listItems = items,
                totalCount = rawItems.size,
                songPlayResultCounts = songPlayResultCounts,
                ratingClassCounts = ratingClassCounts,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(STOP_TIMEOUT.inWholeMilliseconds),
            ListSnapshot(listItems = emptyList(), totalCount = 0, songPlayResultCounts = emptyMap(), ratingClassCounts = emptyMap()),
        )

    val uiState =
        combine(
            isLoading,
            listSnapshot,
            sortOrder,
            sortByValue,
            filter,
        ) { isLoading, snapshot, sortOrder, sortByValue, filter ->
            UiState(
                isLoading = isLoading,
                listItems = snapshot.listItems,
                totalCount = snapshot.totalCount,
                sortOrder = sortOrder,
                sortByValue = sortByValue,
                filter = filter,
                songPlayResultCounts = snapshot.songPlayResultCounts,
                ratingClassCounts = snapshot.ratingClassCounts,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(STOP_TIMEOUT.inWholeMilliseconds),
            initialValue = UiState(),
        )

    fun setSortOrder(value: SortOrder) {
        sortOrder.value = value
    }

    fun setSortByValue(value: SortByValue) {
        sortByValue.value = value
    }

    fun setFilter(value: PlayResultFilter) {
        filter.value = value
    }

    fun setQuery(query: String) {
        filter.update { it.copy(query = query) }
    }

    fun clearFilters() {
        filter.value = PlayResultFilter()
    }

    fun setItemSelected(
        listItem: ListItem,
        selected: Boolean,
    ) {
        if (selected) {
            selectedItemUuids.value += listItem.uuid
        } else {
            selectedItemUuids.value -= listItem.uuid
        }
    }

    /**
     * Select every item currently visible, i.e. after filtering.
     */
    fun selectAllFiltered() {
        selectedItemUuids.value = listItems.value.map { it.uuid }
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

    companion object {
        private val STOP_TIMEOUT = 5.seconds

        private val SEARCH_QUERY_DEBOUNCE = 100.milliseconds
    }
}

/**
 * Resolves the filter to the set of allowed song ids, or null when the filter
 * does not constrain songs at all.
 */
private fun PlayResultFilter.allowedSongIds(
    catalog: ArcaeaCatalog,
    searchedSongIds: Set<String>?,
): Set<String>? {
    if (packIds.isEmpty() && searchedSongIds == null) return null

    var allowed: Set<String>? = null
    if (packIds.isNotEmpty()) allowed = catalog.songIdsOfBasePacks(packIds)
    if (searchedSongIds != null) allowed = allowed?.intersect(searchedSongIds) ?: searchedSongIds

    return allowed
}

private fun DatabasePlayResultListViewModel.ListItem.matches(
    allowedSongIds: Set<String>?,
    query: String,
    catalog: ArcaeaCatalog,
    onlyWarnings: Boolean,
    ratingClasses: Set<ArcaeaRatingClass>,
): Boolean {
    if (ratingClasses.isNotEmpty() && playResult.ratingClass !in ratingClasses) return false
    if (onlyWarnings && warnings.isEmpty()) return false
    if (allowedSongIds != null && playResult.songId !in allowedSongIds) return false
    if (query.isBlank() || !catalog.isLoaded) return true

    // Without song data there is no title to match, so fall back to the id.
    if (catalog.songs.isEmpty()) return playResult.songId.contains(query, ignoreCase = true)

    return true
}
