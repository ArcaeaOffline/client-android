package xyz.sevive.arcaeaoffline.ui.helpers

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.firstOrNull
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.database.entities.ChartInfo
import xyz.sevive.arcaeaoffline.core.database.entities.DifficultyWithSong
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.core.database.repositories.ChartInfoRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.DifficultyWithSongRepository
import kotlin.time.measureTime

// Caches the display data for a set of play results: songlist-driven
// DifficultyWithSong plus the optional ChartInfo. Entries exist for every
// queried (songId, ratingClass) regardless of chart info availability -
// only the ChartInfo part may be null.
class UiDisplayChartCacheHolder {
    data class Entry(
        val difficultyWithSong: DifficultyWithSong,
        val chartInfo: ChartInfo? = null,
    )

    companion object {
        private const val LOG_TAG = "UiDispChartCacheHolder"
    }

    private val logger = Logger.withTag(LOG_TAG)

    private val cache = mutableMapOf<Pair<String, ArcaeaRatingClass>, Entry>()

    suspend fun updateCache(
        keys: List<Pair<String, ArcaeaRatingClass>>,
        difficultyWithSongRepo: DifficultyWithSongRepository,
        chartInfoRepo: ChartInfoRepository,
    ) {
        cache.clear()

        measureTime {
            val keySet = keys.toSet()
            val songIds = keySet.map { it.first }.distinct()

            difficultyWithSongRepo.findAllBySongIds(songIds).firstOrNull()?.forEach { dws ->
                val key = dws.songId to dws.ratingClass
                if (key in keySet) cache[key] = Entry(dws)
            }

            // Single-pass lookup instead of per-key queries.
            chartInfoRepo.findAll().firstOrNull()?.forEach { info ->
                val key = info.songId to info.ratingClass
                cache[key]?.let { cache[key] = it.copy(chartInfo = info) }
            }
        }.let { logger.d { "updateCache took $it" } }
    }

    fun get(playResult: PlayResult): Entry? = get(playResult.songId, playResult.ratingClass)

    fun get(
        songId: String,
        ratingClass: ArcaeaRatingClass,
    ): Entry? = cache[songId to ratingClass]
}
