package xyz.sevive.arcaeaoffline.ui.screens.database.playresultlist

import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultClearStatus
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaScoreGrade
import xyz.sevive.arcaeaoffline.core.search.ArcaeaCatalog
import xyz.sevive.arcaeaoffline.core.search.SearchEngine

/**
 * Play counts per facet option, counted under the other active facets.
 *
 * A facet's own selection is ignored so its options keep showing what
 * selecting them would yield, and the counts do not change while that facet's
 * selection changes.
 */
internal data class PlayResultFacetCounts(
    val ratingClasses: Map<ArcaeaRatingClass, Int> = emptyMap(),
    val scoreGrades: Map<ArcaeaScoreGrade, Int> = emptyMap(),
    val clearStatuses: Map<ArcaeaPlayResultClearStatus, Int> = emptyMap(),
)

/**
 * Resolves the pack facet to the set of allowed song ids, or null when no pack
 * is selected.
 */
internal fun PlayResultFilter.allowedPackSongIds(catalog: ArcaeaCatalog): Set<String>? =
    if (packIds.isEmpty()) null else catalog.songIdsOfBasePacks(packIds)

/**
 * Whether the item passes the facet filters and the text query.
 *
 * [allowedPackSongIds] is a hard filter. [searchedSongIds] narrows only the
 * song dimension of [query]. A comment hit keeps the item regardless.
 */
internal fun PlayResultListUiItem.matches(
    allowedPackSongIds: Set<String>?,
    searchedSongIds: Set<String>?,
    query: String,
    catalog: ArcaeaCatalog,
    filter: PlayResultFilter,
): Boolean {
    if (filter.ratingClasses.isNotEmpty() && playResult.ratingClass !in filter.ratingClasses) return false
    if (filter.scoreGrades.isNotEmpty() && scoreGrade !in filter.scoreGrades) return false

    val status = clearStatus
    if (filter.clearStatuses.isNotEmpty() && (status == null || status !in filter.clearStatuses)) return false

    if (filter.onlyWarnings && warnings.isEmpty()) return false
    if (allowedPackSongIds != null && playResult.songId !in allowedPackSongIds) return false
    if (query.isBlank() || !catalog.isLoaded) return true

    // Without song data there is no title to match, so fall back to the id.
    val songHit =
        if (catalog.songs.isEmpty()) {
            playResult.songId.contains(query, ignoreCase = true)
        } else {
            searchedSongIds?.contains(playResult.songId) == true
        }

    return songHit || matchesComment(query)
}

/**
 * Whether every [SearchEngine.tokenize] token occurs in the comment after
 * [SearchEngine.normalize].
 */
private fun PlayResultListUiItem.matchesComment(query: String): Boolean {
    val comment = playResult.comment ?: return false
    val normalized = SearchEngine.normalize(comment)
    return SearchEngine.tokenize(query).all { it in normalized }
}

internal fun computeFacetCounts(
    items: List<PlayResultListUiItem>,
    filter: PlayResultFilter,
    allowedPackSongIds: Set<String>?,
    searchedSongIds: Set<String>?,
    query: String,
    catalog: ArcaeaCatalog,
): PlayResultFacetCounts {
    val ratingClasses = HashMap<ArcaeaRatingClass, Int>()
    val scoreGrades = HashMap<ArcaeaScoreGrade, Int>()
    val clearStatuses = HashMap<ArcaeaPlayResultClearStatus, Int>()

    val withoutRatingClasses = filter.copy(ratingClasses = emptySet())
    val withoutScoreGrades = filter.copy(scoreGrades = emptySet())
    val withoutClearStatuses = filter.copy(clearStatuses = emptySet())

    items.forEach { item ->
        if (item.matches(allowedPackSongIds, searchedSongIds, query, catalog, withoutRatingClasses)) {
            ratingClasses.merge(item.playResult.ratingClass, 1, Int::plus)
        }

        if (item.matches(allowedPackSongIds, searchedSongIds, query, catalog, withoutScoreGrades)) {
            scoreGrades.merge(item.scoreGrade, 1, Int::plus)
        }

        if (item.matches(allowedPackSongIds, searchedSongIds, query, catalog, withoutClearStatuses)) {
            item.clearStatus?.let { clearStatuses.merge(it, 1, Int::plus) }
        }
    }

    return PlayResultFacetCounts(
        ratingClasses = ratingClasses,
        scoreGrades = scoreGrades,
        clearStatuses = clearStatuses,
    )
}
