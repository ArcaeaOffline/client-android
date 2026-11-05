package xyz.sevive.arcaeaoffline.ui.helpers

import xyz.sevive.arcaeaoffline.core.database.entities.ChartInfo
import xyz.sevive.arcaeaoffline.core.database.entities.DifficultyWithSong

/**
 * Display data for one chart, resolved as one atomic unit.
 *
 * The chart card relies on the pair swapping together: title/level text and constant stay
 * consistent within one frame, and the previous pair is held while the next one is resolving,
 * so the card cross-fades instead of dropping to its placeholder.
 *
 * [chartInfo] is null when the chart is absent from the local chart database.
 */
data class DifficultyWithSongDisplay(
    val difficultyWithSong: DifficultyWithSong,
    val chartInfo: ChartInfo?,
)
