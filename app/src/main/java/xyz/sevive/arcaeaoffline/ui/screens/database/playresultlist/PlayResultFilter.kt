package xyz.sevive.arcaeaoffline.ui.screens.database.playresultlist

import androidx.compose.runtime.Immutable

/**
 * Active constraints of the play result list.
 *
 * The text query narrows the list through the search catalog (title, artist,
 * pack, song id). Songs are a search target, not a filter dimension, so there
 * is no song filter.
 *
 * [packIds] and [onlyWarnings] are independent facets. [onlyWarnings] keeps
 * results that have at least one validation warning.
 */
@Immutable
data class PlayResultFilter(
    val query: String = "",
    val packIds: Set<String> = emptySet(),
    val onlyWarnings: Boolean = false,
) {
    val hasStructuredFilter: Boolean
        get() = packIds.isNotEmpty() || onlyWarnings

    val isActive: Boolean
        get() = query.isNotBlank() || hasStructuredFilter

    /** Number of active filter categories, for the app bar badge. */
    val activeCount: Int
        get() = (if (packIds.isNotEmpty()) 1 else 0) + (if (onlyWarnings) 1 else 0)
}
