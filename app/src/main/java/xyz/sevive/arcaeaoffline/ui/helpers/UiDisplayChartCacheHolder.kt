package xyz.sevive.arcaeaoffline.ui.helpers

import android.util.Log
import kotlinx.coroutines.flow.firstOrNull
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.database.entities.Chart
import xyz.sevive.arcaeaoffline.core.database.entities.Difficulty
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.core.database.helpers.ChartFactory
import xyz.sevive.arcaeaoffline.core.database.repositories.ChartRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.DifficultyRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.SongRepository
import xyz.sevive.arcaeaoffline.ui.containers.ArcaeaOfflineDatabaseRepositoryContainer
import kotlin.time.measureTime

class UiDisplayChartCacheHolder {
    companion object {
        private const val LOG_TAG = "UiDispChartCacheHolder"
    }

    private fun PlayResult.siRcKey() = songId to ratingClass
    private fun Difficulty.siRcKey() = songId to ratingClass
    private fun Chart.siRcKey() = songId to ratingClass

    private val cache = mutableMapOf<Pair<String, ArcaeaRatingClass>, Chart>()

    private suspend fun updateCache(
        playResults: List<PlayResult>,
        songRepo: SongRepository,
        difficultyRepo: DifficultyRepository,
        chartRepo: ChartRepository,
    ) {
        cache.clear()

        measureTime {
            val itemsToQuery = playResults.map { it.siRcKey() }.groupBy { it.first }.map {
                it.key to it.value.map { it.second }.distinct()
            }.toMap().toMutableMap()

            Log.d(LOG_TAG, "${itemsToQuery.size} before chart")
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
            Log.d(LOG_TAG, "${itemsToFake.size} before fake chart")
            // Try faking charts for the remaining items
            for ((songId, ratingClasses) in itemsToFake) {
                val song = songRepo.find(songId).firstOrNull()
                if (song == null) continue

                val difficulties = difficultyRepo.findAllBySongId(songId).firstOrNull() ?: continue
                difficulties.filter { it.ratingClass in ratingClasses }.forEach { difficulty ->
                    cache[difficulty.siRcKey()] = ChartFactory.fakeChart(song, difficulty)
                }
            }
        }.let { Log.d(LOG_TAG, "updateCache took $it") }
    }

    suspend fun updateCache(
        playResults: List<PlayResult>,
        repositoryContainer: ArcaeaOfflineDatabaseRepositoryContainer,
    ) = updateCache(
        playResults,
        repositoryContainer.songRepo,
        repositoryContainer.difficultyRepo,
        repositoryContainer.chartRepo,
    )

    fun get(playResult: PlayResult): Chart? {
        return cache[playResult.siRcKey()]
    }
}
