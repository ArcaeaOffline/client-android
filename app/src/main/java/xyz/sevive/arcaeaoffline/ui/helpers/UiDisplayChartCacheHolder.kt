package xyz.sevive.arcaeaoffline.ui.helpers

import kotlinx.coroutines.flow.firstOrNull
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.database.entities.Chart
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.core.database.helpers.ChartFactory
import xyz.sevive.arcaeaoffline.core.database.repositories.DifficultyRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.SongRepository
import xyz.sevive.arcaeaoffline.ui.containers.ArcaeaOfflineDatabaseRepositoryContainer

class UiDisplayChartCacheHolder {
    private fun PlayResult.siRcKey(): Pair<String, ArcaeaRatingClass> {
        return songId to ratingClass
    }

    private val cache = mutableMapOf<Pair<String, ArcaeaRatingClass>, Chart>()

    private suspend fun updateCache(
        playResults: List<PlayResult>,
        songRepo: SongRepository,
        difficultyRepo: DifficultyRepository,
    ) {
        cache.clear()

        val keys = playResults.map { it.siRcKey() }.distinct()
        keys.forEach { key ->
            val (songId, ratingClass) = key
            val song = songRepo.find(songId).firstOrNull()
            val difficulty = difficultyRepo.find(songId, ratingClass).firstOrNull()

            if (song != null && difficulty != null) {
                cache[key] = ChartFactory.fakeChart(song, difficulty)
            }
        }
    }

    suspend fun updateCache(
        playResults: List<PlayResult>,
        repositoryContainer: ArcaeaOfflineDatabaseRepositoryContainer,
    ) = updateCache(playResults, repositoryContainer.songRepo, repositoryContainer.difficultyRepo)

    fun get(playResult: PlayResult): Chart? {
        return cache[playResult.siRcKey()]
    }
}
