package xyz.sevive.arcaeaoffline.ui.helpers

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.firstOrNull
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.database.entities.Chart
import xyz.sevive.arcaeaoffline.core.database.entities.Difficulty
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.core.database.helpers.ChartFactory
import xyz.sevive.arcaeaoffline.core.database.repositories.ChartRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.DifficultyRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.SongRepository
import kotlin.time.measureTime

class UiDisplayChartCacheHolder {
    companion object {
        private const val LOG_TAG = "UiDispChartCacheHolder"
    }

    private val logger = Logger.withTag(LOG_TAG)

    private fun PlayResult.siRcKey() = songId to ratingClass

    private fun Difficulty.siRcKey() = songId to ratingClass

    private fun Chart.siRcKey() = songId to ratingClass

    private val cache = mutableMapOf<Pair<String, ArcaeaRatingClass>, Chart>()

    suspend fun updateCache(
        playResults: List<PlayResult>,
        songRepo: SongRepository,
        difficultyRepo: DifficultyRepository,
        chartRepo: ChartRepository,
    ) {
        cache.clear()

        measureTime {
            val itemsToQuery =
                playResults
                    .map { it.siRcKey() }
                    .groupBy { it.first }
                    .map {
                        it.key to it.value.map { it.second }.distinct()
                    }.toMap()
                    .toMutableMap()

            logger.d { "${itemsToQuery.size} before chart" }
            // Consider normal charts first, then other screens may use these chart
            // for play results validation or other functions.
            chartRepo.findAllBySongIds(itemsToQuery.keys.toList()).firstOrNull()?.forEach { chart ->
                val ratingClasses = itemsToQuery[chart.songId]
                if (ratingClasses != null && chart.ratingClass in ratingClasses) {
                    cache[chart.siRcKey()] = chart.copy()
                    // remove found items' keys
                    itemsToQuery[chart.songId] = ratingClasses - chart.ratingClass
                }
            }

            val itemsToFake = itemsToQuery.filter { it.value.isNotEmpty() }
            logger.d { "${itemsToFake.size} before fake chart" }
            // Try faking charts for the remaining items
            for ((songId, ratingClasses) in itemsToFake) {
                val song = songRepo.find(songId).firstOrNull()
                if (song == null) continue

                val difficulties = difficultyRepo.findAllBySongId(songId).firstOrNull() ?: continue
                difficulties.filter { it.ratingClass in ratingClasses }.forEach { difficulty ->
                    cache[difficulty.siRcKey()] = ChartFactory.fakeChart(song, difficulty)
                }
            }
        }.let { logger.d { "updateCache took $it" } }
    }

    fun get(playResult: PlayResult): Chart? = cache[playResult.siRcKey()]
}
