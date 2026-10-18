package xyz.sevive.arcaeaoffline.ui.screens.database.playresultlist

import androidx.compose.runtime.Immutable

/**
 * Active constraints of the play result list.
 *
 * The text query narrows the list through the search catalog (title, artist,
 * pack, song id). [packIds] is an independent facet. Songs are a search
 * target, not a filter dimension, so there is no song filter.
 */
@Immutable
data class PlayResultFilter(
    val query: String = "",
    val packIds: Set<String> = emptySet(),
) {
    val hasStructuredFilter: Boolean
        get() = packIds.isNotEmpty()

    val isActive: Boolean
        get() = query.isNotBlank() || hasStructuredFilter

    /** Number of active filter categories, for the app bar badge. */
    val activeCount: Int
        get() = if (packIds.isNotEmpty()) 1 else 0
}
