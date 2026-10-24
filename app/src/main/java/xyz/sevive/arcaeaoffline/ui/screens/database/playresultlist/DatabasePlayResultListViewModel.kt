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
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultClearStatus
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaScoreGrade
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

    enum class SortByValue { TITLE, DIFFICULTY, SCORE, PLAY_RATING, DATE }

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
        val scoreGrade = ArcaeaScoreGrade.of(playResult.score)
        val clearStatus = ArcaeaPlayResultClearStatus.of(playResult.clearType)

        val title: String = display?.difficultyWithSong?.title ?: playResult.songId

        private val chartConstant: Int? = display?.chartInfo?.constant?.takeIf { it > 0 }

        /**
         * Difficulty key in tenths, used for sorting.
         *
         * A chart without a constant falls back to the top of its displayed
         * level. Null means the song data is missing, so the chart has no level.
         */
        val difficultySortValue: Int? =
            chartConstant
                ?: display?.difficultyWithSong?.let { levelTopConstant(it.rating, it.ratingPlus) }

        /**
         * Whether [difficultySortValue] comes from chart info rather than the
         * level fallback.
         */
        val hasConstant: Boolean = chartConstant != null
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
        val scoreGradeCounts: Map<ArcaeaScoreGrade, Int> = emptyMap(),
        val clearStatusCounts: Map<ArcaeaPlayResultClearStatus, Int> = emptyMap(),
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
     * [songIds] is null when the query does not constrain the list, which
     * happens when the query is empty, the catalog is not loaded, or no song
     * data is imported (the list then falls back to matching the raw song id).
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
            items.filter { it.matches(allowedSongIds, search.query, catalog, filter) }
        }.flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT.inWholeMilliseconds), emptyList())

    /**
     * Play results per facet option, counted under the other active facets.
     *
     * A facet's own selection is ignored so its options keep showing what
     * selecting them would yield, and the counts do not change while that
     * facet's selection changes.
     */
    private data class FacetCounts(
        val ratingClasses: Map<ArcaeaRatingClass, Int> = emptyMap(),
        val scoreGrades: Map<ArcaeaScoreGrade, Int> = emptyMap(),
        val clearStatuses: Map<ArcaeaPlayResultClearStatus, Int> = emptyMap(),
    )

    private val facetCounts: StateFlow<FacetCounts> =
        combine(rawListItems, filter, songSearchState, catalog) { items, filter, search, catalog ->
            val allowedSongIds = filter.allowedSongIds(catalog, search.songIds)
            val ratingClasses = HashMap<ArcaeaRatingClass, Int>()
            val scoreGrades = HashMap<ArcaeaScoreGrade, Int>()
            val clearStatuses = HashMap<ArcaeaPlayResultClearStatus, Int>()

            val withoutRatingClasses = filter.copy(ratingClasses = emptySet())
            val withoutScoreGrades = filter.copy(scoreGrades = emptySet())
            val withoutClearStatuses = filter.copy(clearStatuses = emptySet())

            items.forEach { item ->
                if (item.matches(allowedSongIds, search.query, catalog, withoutRatingClasses)) {
                    ratingClasses.merge(item.playResult.ratingClass, 1, Int::plus)
                }

                if (item.matches(allowedSongIds, search.query, catalog, withoutScoreGrades)) {
                    scoreGrades.merge(item.scoreGrade, 1, Int::plus)
                }

                if (item.matches(allowedSongIds, search.query, catalog, withoutClearStatuses)) {
                    item.clearStatus?.let { clearStatuses.merge(it, 1, Int::plus) }
                }
            }

            FacetCounts(
                ratingClasses = ratingClasses,
                scoreGrades = scoreGrades,
                clearStatuses = clearStatuses,
            )
        }.flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT.inWholeMilliseconds), FacetCounts())

    private val listItems: StateFlow<List<ListItem>> =
        combine(filteredListItems, sortOrder, sortByValue) { items, order, sortBy ->
            when (sortBy) {
                SortByValue.TITLE -> items.sortedWith(titleComparator(order))
                SortByValue.DIFFICULTY -> items.sortedWith(difficultyComparator(order))
                SortByValue.SCORE -> items.sortedByValue(order) { it.playResult.score }
                SortByValue.PLAY_RATING -> items.sortedByValue(order) { it.playRating }
                SortByValue.DATE -> items.sortedByValue(order) { it.playResult.date?.toEpochMilliseconds() }
            }
        }.flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT.inWholeMilliseconds), emptyList())

    private data class ListSnapshot(
        val listItems: List<ListItem>,
        val totalCount: Int,
        val songPlayResultCounts: Map<String, Int>,
        val facetCounts: FacetCounts,
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
                facetCounts = FacetCounts(),
            ),
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
                ratingClassCounts = snapshot.facetCounts.ratingClasses,
                scoreGradeCounts = snapshot.facetCounts.scoreGrades,
                clearStatusCounts = snapshot.facetCounts.clearStatuses,
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

    private fun titleComparator(order: SortOrder): Comparator<ListItem> {
        val byTitle = compareBy<ListItem, String>(String.CASE_INSENSITIVE_ORDER) { it.title }
        return if (order == SortOrder.ASC) byTitle else byTitle.reversed()
    }

    private fun difficultyComparator(order: SortOrder): Comparator<ListItem> {
        val byMissing = compareBy<ListItem> { it.difficultySortValue == null }
        val byValue = compareBy<ListItem> { it.difficultySortValue ?: 0 }
        // A fallback value ties with the level's highest known constant; this
        // orders the chart without a constant above that known constant.
        val byKnown = compareBy<ListItem> { !it.hasConstant }
        val byTitle = compareBy<ListItem, String>(String.CASE_INSENSITIVE_ORDER) { it.title }

        return if (order == SortOrder.ASC) {
            byMissing.then(byValue).then(byKnown).then(byTitle)
        } else {
            byMissing.then(byValue.reversed()).then(byKnown.reversed()).then(byTitle)
        }
    }

    private fun <T : Comparable<T>> List<ListItem>.sortedByValue(
        order: SortOrder,
        selector: (ListItem) -> T?,
    ): List<ListItem> {
        val sorted = sortedBy(selector)
        return if (order == SortOrder.ASC) sorted else sorted.reversed()
    }

    companion object {
        private val STOP_TIMEOUT = 5.seconds

        private val SEARCH_QUERY_DEBOUNCE = 100.milliseconds
    }
}

/**
 * Fallback difficulty key for a chart whose constant is missing, in tenths.
 *
 * Uses the .6/.9 split of the level's tenths. Where the real constants use a
 * coarser grid, the value lands in the gap above the highest real constant.
 */
private fun levelTopConstant(
    rating: Int,
    ratingPlus: Boolean,
): Int = rating * 10 + if (ratingPlus) 9 else 6

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
    filter: PlayResultFilter,
): Boolean {
    if (filter.ratingClasses.isNotEmpty() && playResult.ratingClass !in filter.ratingClasses) return false
    if (filter.scoreGrades.isNotEmpty() && scoreGrade !in filter.scoreGrades) return false

    val status = clearStatus
    if (filter.clearStatuses.isNotEmpty() && (status == null || status !in filter.clearStatuses)) return false

    if (filter.onlyWarnings && warnings.isEmpty()) return false
    if (allowedSongIds != null && playResult.songId !in allowedSongIds) return false
    if (query.isBlank() || !catalog.isLoaded) return true

    // Without song data there is no title to match, so fall back to the id.
    if (catalog.songs.isEmpty()) return playResult.songId.contains(query, ignoreCase = true)

    return true
}
