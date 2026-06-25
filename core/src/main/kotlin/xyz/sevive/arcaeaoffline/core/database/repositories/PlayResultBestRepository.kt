package xyz.sevive.arcaeaoffline.core.database.repositories

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import xyz.sevive.arcaeaoffline.core.calculators.calculatePlayRating
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.database.daos.RelationshipsDao
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResultCalculated

interface PlayResultBestRepository {
    fun find(
        songId: String,
        ratingClass: ArcaeaRatingClass,
    ): Flow<PlayResultCalculated?>

    fun orderDescWithLimit(limit: Int): Flow<List<PlayResultCalculated>>
}

@OptIn(ExperimentalCoroutinesApi::class)
class PlayResultBestRepositoryImpl(
    private val relationshipsDao: RelationshipsDao,
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

    override fun orderDescWithLimit(limit: Int): Flow<List<PlayResultCalculated>> =
        relationshipsDao.minimumPlayResultPotentialFields().flatMapLatest { originalList ->
            val topUuids =
                originalList
                    .groupBy { it.songId to it.ratingClass }
                    .values
                    .map { group ->
                        // Calculate play rating once and find the best
                        group
                            .map { it to calculatePlayRating(it.score, it.constant) }
                            .maxBy { it.second }
                    }
                    // Sort all best results by play rating
                    .sortedByDescending { it.second }
                    .take(limit)
                    .map { it.first.uuid }

            playResultCalculatedRepo.findAllByUuid(topUuids).map { list ->
                // Re-sort because the DB might return them in a different order
                list.sortedByDescending { it.playRating }
            }
        }
}
