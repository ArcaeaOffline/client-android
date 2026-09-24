package xyz.sevive.arcaeaoffline.core.database.r30

import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultClearType
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultModifier
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.database.entities.ChartInfo
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.core.database.entities.playRating
import xyz.sevive.arcaeaoffline.core.database.repositories.R30EntryCombined

/** Identifies one chart: the unit the recent queue counts distinct entries by. */
data class ChartKey(
    val songId: String,
    val ratingClass: ArcaeaRatingClass,
)

/** Best score of a chart before the play being written; absent while the chart has no record. */
typealias ChartBestScores = Map<ChartKey, Int>

/**
 * Rebuilds the recent-30 queue under the B30 + R10 rules from play results.
 *
 * [replay] applies one play at a time, in play order:
 * * a play enters the queue directly, evicting the entry with the earliest play time, or
 *   conditionally, evicting the entry with the lowest single-play potential and being discarded
 *   when no entry is lower;
 * * conditional writes are triggered by an EX-or-better score, a hard-mode Track Lost, and a
 *   score above the chart's previous record, the last of which also covers a chart's first
 *   submission;
 * * a play whose direct write would take the queue below ten distinct charts, or lower that
 *   count, is written conditionally instead; a write that still breaks the floor is dropped.
 */
class R30QueueUpdater(
    private val chartInfoOf: suspend (PlayResult) -> ChartInfo?,
) {
    /**
     * Applies [plays] in order onto [entries].
     *
     * [previousBestScores] carries the chart records of plays that came before [plays], so an
     * incremental run sees what a rebuild from the whole history would.
     */
    suspend fun replay(
        plays: List<PlayResult>,
        entries: List<R30EntryCombined> = emptyList(),
        previousBestScores: ChartBestScores = emptyMap(),
        onPlay: suspend () -> Unit = {},
    ): List<R30EntryCombined> {
        var queue = entries
        val bestScores = previousBestScores.toMutableMap()

        plays.forEach { play ->
            val chart = ChartKey(play.songId, play.ratingClass)
            queue = update(queue, play, chartInfoOf(play), bestScores[chart])

            // The record advances even for a play that never enters the queue.
            bestScores[chart] = maxOf(bestScores[chart] ?: 0, play.score)
            onPlay()
        }

        return queue
    }

    private fun update(
        entries: List<R30EntryCombined>,
        playResult: PlayResult,
        chartInfo: ChartInfo?,
        previousBestScore: Int?,
    ): List<R30EntryCombined> {
        val newEntry = R30EntryCombined.build(playResult, chartInfo)
        if (entries.size < QUEUE_SIZE) return entries + newEntry

        val conditionally = triggersConditionalWrite(playResult, previousBestScore)
        val written = write(entries, playResult, chartInfo, newEntry, conditionally) ?: return entries

        val distinctBefore = distinctCharts(entries)
        val distinctAfter = distinctCharts(written)
        if (distinctAfter >= DISTINCT_CHART_FLOOR && distinctAfter >= distinctBefore) return written

        val retried = write(entries, playResult, chartInfo, newEntry, conditionally = true) ?: return entries
        return if (distinctCharts(retried) < DISTINCT_CHART_FLOOR) entries else retried
    }

    /**
     * Null when the play is dropped instead of written: a conditional write needs chart info and
     * a potential at or above the queue's lowest.
     */
    private fun write(
        entries: List<R30EntryCombined>,
        playResult: PlayResult,
        chartInfo: ChartInfo?,
        newEntry: R30EntryCombined,
        conditionally: Boolean,
    ): List<R30EntryCombined>? = if (conditionally) conditionalWrite(entries, playResult, chartInfo) else directWrite(entries, newEntry)

    private fun conditionalWrite(
        entries: List<R30EntryCombined>,
        playResult: PlayResult,
        chartInfo: ChartInfo?,
    ): List<R30EntryCombined>? {
        // A play without chart info has no potential to compare against the queue.
        if (chartInfo == null) return null

        val lowest = entries.minByOrNull { it.playRating() ?: Double.MAX_VALUE } ?: return null
        val lowestRating = lowest.playRating() ?: return null
        if (playResult.playRating(chartInfo) < lowestRating) return null

        return entries - lowest + R30EntryCombined.build(playResult, chartInfo)
    }

    private fun directWrite(
        entries: List<R30EntryCombined>,
        newEntry: R30EntryCombined,
    ): List<R30EntryCombined> {
        val earliest =
            entries.minByOrNull { it.playResult.date?.toEpochMilliseconds() ?: Long.MAX_VALUE } ?: return entries

        return entries - earliest + newEntry
    }

    private fun triggersConditionalWrite(
        playResult: PlayResult,
        previousBestScore: Int?,
    ): Boolean {
        if (playResult.score >= EX_SCORE) return true
        if (playResult.clearType == ArcaeaPlayResultClearType.TRACK_LOST && playResult.modifier == ArcaeaPlayResultModifier.HARD) {
            return true
        }

        return previousBestScore == null || playResult.score > previousBestScore
    }

    private fun distinctCharts(entries: List<R30EntryCombined>): Int =
        entries.distinctBy { ChartKey(it.playResult.songId, it.playResult.ratingClass) }.size

    private companion object {
        const val QUEUE_SIZE = 30
        const val DISTINCT_CHART_FLOOR = 10
        const val EX_SCORE = 9_800_000
    }
}
