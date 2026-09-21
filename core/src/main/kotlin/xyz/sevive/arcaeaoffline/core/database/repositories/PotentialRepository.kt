package xyz.sevive.arcaeaoffline.core.database.repositories

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaScoringMode
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResultCalculated

interface PotentialRepository {
    /** Average single-play potential of the best 30 entries (legacy display value). */
    fun b30(): Flow<Double>

    /** Average single-play potential of the recent top 10 entries (legacy display value). */
    fun r10(): Flow<Double>

    /** Average single-play potential of the best 50 entries (B50 display value). */
    fun b50(): Flow<Double>

    /** Average single-play potential of the top 10 of the best 50 entries (B50 display value). */
    fun b10(): Flow<Double>

    /** Overall potential, following the scoring mode stored in the database properties. */
    fun potential(): Flow<Double>
}

@OptIn(ExperimentalCoroutinesApi::class)
class PotentialRepositoryImpl(
    private val playResultBestRepo: PlayResultBestRepository,
    private val r30EntryRepo: R30EntryRepository,
    private val propertyRepo: PropertyRepository,
) : PotentialRepository {
    private fun b30Entries(): Flow<List<PlayResultCalculated>> = playResultBestRepo.orderDescWithLimit(30, ArcaeaScoringMode.B30_R10)

    private fun b50Entries(): Flow<List<PlayResultCalculated>> = playResultBestRepo.orderDescWithLimit(50, ArcaeaScoringMode.B50)

    private fun r10Entries(): Flow<List<R30EntryCombined>> =
        r30EntryRepo.findAllCombined().mapLatest {
            it.sortedByDescending { it.playRating() ?: -1.0 }.take(10)
        }

    override fun b30() =
        this.b30Entries().mapLatest { entries ->
            if (entries.isEmpty()) {
                0.0
            } else {
                entries.sumOf { it.playRating } / entries.size
            }
        }

    override fun r10() =
        this.r10Entries().mapLatest { entries ->
            if (entries.isEmpty()) {
                0.0
            } else {
                entries.sumOf { it.playRating() ?: 0.0 } / entries.size
            }
        }

    override fun b50() =
        this.b50Entries().mapLatest { entries ->
            if (entries.isEmpty()) {
                0.0
            } else {
                entries.sumOf { it.playRatingWithClearBonus } / entries.size
            }
        }

    override fun b10() =
        this.b50Entries().mapLatest { entries ->
            val top = entries.take(10)
            if (top.isEmpty()) {
                0.0
            } else {
                top.sumOf { it.playRatingWithClearBonus } / top.size
            }
        }

    override fun potential() =
        propertyRepo.scoringMode().flatMapLatest { mode ->
            when (mode) {
                ArcaeaScoringMode.B30_R10 -> {
                    combine(b30(), r10()) { b30, r10 ->
                        b30 * 0.75 + r10 * 0.25
                    }
                }

                ArcaeaScoringMode.B50 -> {
                    // Official v7.0 formula: the best 10 entries count twice,
                    // i.e. (best50 sum + best10 sum) / 60
                    b50Entries().mapLatest { entries ->
                        val b50Sum = entries.sumOf { it.playRatingWithClearBonus }
                        val b10Sum = entries.take(10).sumOf { it.playRatingWithClearBonus }
                        (b50Sum + b10Sum) / 60
                    }
                }
            }
        }
}
