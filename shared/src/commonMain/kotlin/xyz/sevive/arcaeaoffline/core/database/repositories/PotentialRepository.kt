package xyz.sevive.arcaeaoffline.core.database.repositories

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaScoringMode
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResultCalculated
import xyz.sevive.arcaeaoffline.core.database.r30.ChartKey

data class PotentialGroup<T>(
    val items: List<T>,
    val total: Double,
    val isComplete: Boolean,
)

data class PotentialGroups(
    val b10: PotentialGroup<PlayResultCalculated>,
    val b50: PotentialGroup<PlayResultCalculated>,
    val b30: PotentialGroup<PlayResultCalculated>,
    val r10: PotentialGroup<R30EntryCombined>,
)

interface PotentialRepository {
    fun groups(): Flow<PotentialGroups>
}

@OptIn(ExperimentalCoroutinesApi::class)
class PotentialRepositoryImpl(
    private val playResultBestRepo: PlayResultBestRepository,
    private val r30EntryRepo: R30EntryRepository,
) : PotentialRepository {
    private fun b30Entries(): Flow<List<PlayResultCalculated>> = playResultBestRepo.orderDescWithLimit(30, ArcaeaScoringMode.B30_R10)

    private fun b50Entries(): Flow<List<PlayResultCalculated>> = playResultBestRepo.orderDescWithLimit(50, ArcaeaScoringMode.B50)

    private fun r10Entries(): Flow<List<R30EntryCombined>> =
        r30EntryRepo.findAllCombined().mapLatest { entries ->
            // One entry per chart: the highest-rated of its plays in the queue
            entries
                .sortedByDescending { it.playRating() ?: -1.0 }
                .distinctBy { ChartKey(it.playResult.songId, it.playResult.ratingClass) }
                .take(10)
        }

    override fun groups(): Flow<PotentialGroups> =
        combine(b30Entries(), b50Entries(), r10Entries()) { b30, b50, r10 ->
            val b10 = b50.take(10)

            PotentialGroups(
                b10 =
                    PotentialGroup(
                        items = b10,
                        total = b10.sumOf { it.playRatingWithClearBonus },
                        isComplete = b10.count() >= 10,
                    ),
                b50 =
                    PotentialGroup(
                        items = b50,
                        total = b50.sumOf { it.playRatingWithClearBonus },
                        isComplete = b50.count() >= 50,
                    ),
                b30 =
                    PotentialGroup(
                        items = b30,
                        total = b30.sumOf { it.playRating },
                        isComplete = b30.count() >= 30,
                    ),
                r10 =
                    PotentialGroup(
                        items = r10,
                        total = r10.sumOf { it.playRating() ?: 0.0 },
                        isComplete = r10.count() >= 10,
                    ),
            )
        }
}
