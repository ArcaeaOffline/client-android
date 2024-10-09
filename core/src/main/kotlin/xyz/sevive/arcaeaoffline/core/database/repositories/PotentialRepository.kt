package xyz.sevive.arcaeaoffline.core.database.repositories

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResultBest

interface PotentialRepository {
    fun b30(): Flow<Double>
    fun r10(): Flow<Double>
}

class PotentialRepositoryImpl(
    private val playResultBestRepo: PlayResultBestRepository,
    private val r30EntryRepo: R30EntryRepository
) : PotentialRepository {
    private fun b30Entries(): Flow<List<PlayResultBest>> =
        playResultBestRepo.orderDescWithLimit(30)

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun r10Entries(): Flow<List<R30EntryCombined>> =
        r30EntryRepo.findAllCombined().mapLatest {
            it.sortedByDescending { it.potential() ?: -1.0 }.take(10)
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun b30() = this.b30Entries().mapLatest { entries ->
        if (entries.isEmpty()) 0.0
        else entries.sumOf { it.potential } / entries.size
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun r10() = this.r10Entries().mapLatest { entries ->
        if (entries.isEmpty()) 0.0
        else entries.sumOf { it.potential() ?: 0.0 } / entries.size
    }
}
