package xyz.sevive.arcaeaoffline.core.database.repositories

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import xyz.sevive.arcaeaoffline.core.calculators.calculatePlayRating
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaScoringMode
import xyz.sevive.arcaeaoffline.core.database.daos.PlayResultBestDao
import xyz.sevive.arcaeaoffline.core.database.entities.MinimumPlayResultPotentialFields
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResultCalculated

interface PlayResultBestRepository {
    fun find(
        songId: String,
        ratingClass: ArcaeaRatingClass,
    ): Flow<PlayResultCalculated?>

    fun orderDescWithLimit(
        limit: Int,
        scoringMode: ArcaeaScoringMode,
    ): Flow<List<PlayResultCalculated>>
}

@OptIn(ExperimentalCoroutinesApi::class)
class PlayResultBestRepositoryImpl(
    private val playResultBestDao: PlayResultBestDao,
    private val playResultCalculatedRepo: PlayResultCalculatedRepository,
) : PlayResultBestRepository {
    override fun find(
        songId: String,
        ratingClass: ArcaeaRatingClass,
    ): Flow<PlayResultCalculated?> =
        playResultCalculatedRepo
            .findAllBySongIdAndRatingClass(songId, ratingClass)
            .mapLatest { list ->
                list.maxByOrNull { it.playRating }
            }

    override fun orderDescWithLimit(
        limit: Int,
        scoringMode: ArcaeaScoringMode,
    ): Flow<List<PlayResultCalculated>> =
        playResultBestDao.minimumPlayResultPotentialFields().flatMapLatest { originalList ->
            val topUuids =
                originalList
                    .groupBy { it.songId to it.ratingClass }
                    .values
                    .map { group ->
                        // Calculate play rating once and find the best
                        group
                            .map { it to it.playRating(scoringMode) }
                            .maxBy { it.second }
                    }
                    // Sort all best results by play rating
                    .sortedByDescending { it.second }
                    .take(limit)
                    .map { it.first.uuid }

            playResultCalculatedRepo.findAllByUuid(topUuids).map { list ->
                // Re-sort because the DB might return them in a different order
                list.sortedByDescending { it.playRating(scoringMode) }
            }
        }

    /**
     * B50 ranks by the clear-bonus-inclusive play rating, so a cleared play can
     * outrank a higher-scoring TRACK_LOST play of the same chart.
     */
    private fun MinimumPlayResultPotentialFields.playRating(scoringMode: ArcaeaScoringMode): Double =
        when (scoringMode) {
            ArcaeaScoringMode.B30_R10 -> calculatePlayRating(score, constant)
            ArcaeaScoringMode.B50 -> calculatePlayRating(score, constant, clearType)
        }

    private fun PlayResultCalculated.playRating(scoringMode: ArcaeaScoringMode): Double =
        when (scoringMode) {
            ArcaeaScoringMode.B30_R10 -> playRating
            ArcaeaScoringMode.B50 -> playRatingWithClearBonus
        }
}
