package xyz.sevive.arcaeaoffline.ui.screens.database.playresultlist

import androidx.compose.runtime.Immutable
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultClearStatus
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaScoreGrade

/**
 * Active constraints of the play result list.
 *
 * The text query narrows the list through the search catalog (title, artist,
 * pack, song id). Songs are a search target, not a filter dimension, so there
 * is no song filter.
 *
 * [packIds], [ratingClasses], [scoreGrades], [clearStatuses] and [onlyWarnings]
 * are independent facets. [onlyWarnings] keeps results that have at least one
 * validation warning.
 */
@Immutable
data class PlayResultFilter(
    val query: String = "",
    val packIds: Set<String> = emptySet(),
    val ratingClasses: Set<ArcaeaRatingClass> = emptySet(),
    val scoreGrades: Set<ArcaeaScoreGrade> = emptySet(),
    val clearStatuses: Set<ArcaeaPlayResultClearStatus> = emptySet(),
    val onlyWarnings: Boolean = false,
) {
    /** Number of active filter categories, for the app bar badge. */
    val activeCount: Int
        get() =
            listOf(
                packIds.isNotEmpty(),
                ratingClasses.isNotEmpty(),
                scoreGrades.isNotEmpty(),
                clearStatuses.isNotEmpty(),
                onlyWarnings,
            ).count { it }

    val hasStructuredFilter: Boolean
        get() = activeCount > 0

    val isActive: Boolean
        get() = query.isNotBlank() || hasStructuredFilter
}
