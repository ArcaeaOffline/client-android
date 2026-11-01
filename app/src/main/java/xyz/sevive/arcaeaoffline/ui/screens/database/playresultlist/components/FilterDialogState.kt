package xyz.sevive.arcaeaoffline.ui.screens.database.playresultlist.components

import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultClearStatus
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaScoreGrade
import xyz.sevive.arcaeaoffline.core.search.ArcaeaCatalog
import xyz.sevive.arcaeaoffline.ui.screens.database.playresultlist.PlayResultFilter

/**
 * Filter values, catalog and derived counts shared by every dialog screen.
 *
 * Screens mutate filters through [onFilterChange] via the setters below, so no
 * per-facet callback has to be threaded through the screen hierarchy.
 */
internal data class FilterDialogState(
    val filter: PlayResultFilter,
    val catalog: ArcaeaCatalog,
    val songPlayResultCounts: Map<String, Int>,
    val ratingClassCounts: Map<ArcaeaRatingClass, Int>,
    val scoreGradeCounts: Map<ArcaeaScoreGrade, Int>,
    val clearStatusCounts: Map<ArcaeaPlayResultClearStatus, Int>,
    val resultCount: Int,
    val onFilterChange: (PlayResultFilter) -> Unit,
) {
    fun setPackIds(packIds: Set<String>) {
        onFilterChange(filter.copy(packIds = packIds))
    }

    fun setRatingClasses(ratingClasses: Set<ArcaeaRatingClass>) {
        onFilterChange(filter.copy(ratingClasses = ratingClasses))
    }

    fun setScoreGrades(scoreGrades: Set<ArcaeaScoreGrade>) {
        onFilterChange(filter.copy(scoreGrades = scoreGrades))
    }

    fun setClearStatuses(clearStatuses: Set<ArcaeaPlayResultClearStatus>) {
        onFilterChange(filter.copy(clearStatuses = clearStatuses))
    }

    fun reset() {
        onFilterChange(PlayResultFilter(query = filter.query))
    }
}
