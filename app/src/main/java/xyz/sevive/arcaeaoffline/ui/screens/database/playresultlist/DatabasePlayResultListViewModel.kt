package xyz.sevive.arcaeaoffline.ui.screens.database.playresultlist

import android.content.Context
import androidx.compose.material3.SnackbarHostState
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
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultClearStatus
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaScoreGrade
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.core.database.repositories.ChartInfoRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.DifficultyWithSongRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.PlayResultRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.PropertyRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.SongRepository
import xyz.sevive.arcaeaoffline.core.search.ArcaeaCatalog
import xyz.sevive.arcaeaoffline.core.search.ArcaeaCatalogStateHolder
import xyz.sevive.arcaeaoffline.core.search.SearchEngine
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
    data class UiState(
        val isLoading: Boolean = true,
        val sortOrder: PlayResultSortOrder = PlayResultSortOrder.DESC,
        val sortKey: PlayResultSortKey = PlayResultSortKey.DATE,
        val filter: PlayResultFilter = PlayResultFilter(),
        val totalCount: Int = 0,
        val listItems: List<PlayResultListUiItem> = emptyList(),
        val songPlayResultCounts: Map<String, Int> = emptyMap(),
        val ratingClassCounts: Map<ArcaeaRatingClass, Int> = emptyMap(),
        val scoreGradeCounts: Map<ArcaeaScoreGrade, Int> = emptyMap(),
        val clearStatusCounts: Map<ArcaeaPlayResultClearStatus, Int> = emptyMap(),
    )

    private val isLoading = MutableStateFlow(true)
    private val sortOrder = MutableStateFlow(PlayResultSortOrder.DESC)
    private val sortKey = MutableStateFlow(PlayResultSortKey.DATE)
    val selectedItemUuids = MutableStateFlow(emptySet<Uuid>())

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

                    PlayResultListUiItem(
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
     * [songIds] is null when the query is empty, the catalog is not loaded, or
     * no song data is imported. In the last case the song dimension falls back
     * to matching the raw song id. While the catalog is not loaded the query
     * does not constrain the list at all.
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

    private val filteredListItems: StateFlow<List<PlayResultListUiItem>> =
        combine(rawListItems, filter, songSearchState, catalog) { items, filter, search, catalog ->
            val allowedPackSongIds = filter.allowedPackSongIds(catalog)
            items.filter { it.matches(allowedPackSongIds, search.songIds, search.query, catalog, filter) }
        }.flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT.inWholeMilliseconds), emptyList())

    private val facetCounts: StateFlow<PlayResultFacetCounts> =
        combine(rawListItems, filter, songSearchState, catalog) { items, filter, search, catalog ->
            val allowedPackSongIds = filter.allowedPackSongIds(catalog)
            computeFacetCounts(items, filter, allowedPackSongIds, search.songIds, search.query, catalog)
        }.flowOn(Dispatchers.Default)
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(STOP_TIMEOUT.inWholeMilliseconds),
                PlayResultFacetCounts(),
            )

    private val listItems: StateFlow<List<PlayResultListUiItem>> =
        combine(filteredListItems, sortOrder, sortKey) { items, order, key ->
            sortPlayResultList(items, order, key)
        }.flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT.inWholeMilliseconds), emptyList())

    private data class ListSnapshot(
        val listItems: List<PlayResultListUiItem>,
        val totalCount: Int,
        val songPlayResultCounts: Map<String, Int>,
        val facetCounts: PlayResultFacetCounts,
    )

    private val listSnapshot: StateFlow<ListSnapshot> =
        combine(listItems, rawListItems, facetCounts) { items, rawItems, facetCounts ->
            val songPlayResultCounts = HashMap<String, Int>(rawItems.size)
            rawItems.forEach { songPlayResultCounts.merge(it.playResult.songId, 1, Int::plus) }

            ListSnapshot(
                listItems = items,
                totalCount = rawItems.size,
                songPlayResultCounts = songPlayResultCounts,
                facetCounts = facetCounts,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(STOP_TIMEOUT.inWholeMilliseconds),
            ListSnapshot(
                listItems = emptyList(),
                totalCount = 0,
                songPlayResultCounts = emptyMap(),
                facetCounts = PlayResultFacetCounts(),
            ),
        )

    val uiState =
        combine(
            isLoading,
            listSnapshot,
            sortOrder,
            sortKey,
            filter,
        ) { isLoading, snapshot, sortOrder, sortKey, filter ->
            UiState(
                isLoading = isLoading,
                listItems = snapshot.listItems,
                totalCount = snapshot.totalCount,
                sortOrder = sortOrder,
                sortKey = sortKey,
                filter = filter,
                songPlayResultCounts = snapshot.songPlayResultCounts,
                ratingClassCounts = snapshot.facetCounts.ratingClasses,
                scoreGradeCounts = snapshot.facetCounts.scoreGrades,
                clearStatusCounts = snapshot.facetCounts.clearStatuses,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(STOP_TIMEOUT.inWholeMilliseconds),
            initialValue = UiState(),
        )

    fun setSortOrder(value: PlayResultSortOrder) {
        sortOrder.value = value
    }

    fun setSortKey(value: PlayResultSortKey) {
        sortKey.value = value
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
        listItem: PlayResultListUiItem,
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
        selectedItemUuids.value = listItems.value.mapTo(HashSet()) { it.uuid }
    }

    /**
     * **DELETE** selected items **IN DATABASE**.
     */
    fun deleteSelectedItemsInDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            val playResults =
                playResultRepo
                    .findAllByUuid(selectedItemUuids.value.toList())
                    .firstOrNull() ?: emptyList()

            playResultRepo.deleteBatch(*playResults.toTypedArray())
            clearSelectedItems()
        }
    }

    /**
     * Set all uiItems to unselected state.
     */
    fun clearSelectedItems() {
        selectedItemUuids.value = emptySet()
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
