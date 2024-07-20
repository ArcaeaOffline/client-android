package xyz.sevive.arcaeaoffline.core.database.repositories

import kotlinx.coroutines.flow.firstOrNull
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResultBest

interface PotentialRepository {
    suspend fun b30(): Double
    suspend fun r10(): Double
    suspend fun potential(): Double
}

class PotentialRepositoryImpl(
    private val playResultBestRepo: PlayResultBestRepository,
    private val r30EntryRepo: R30EntryRepository
) : PotentialRepository {
    private suspend fun b30Entries(): List<PlayResultBest> {
        return playResultBestRepo.orderDescWithLimit(30).firstOrNull() ?: listOf()
    }

    private suspend fun r10Entries(): List<R30EntryCombined> {
        val entries = r30EntryRepo.findAllCombined().firstOrNull() ?: return listOf()

        return entries.sortedByDescending { it.potential() ?: -1.0 }.take(10)
    }

    override suspend fun b30(): Double {
        val entries = this.b30Entries()
        if (entries.isEmpty()) return 0.0
        return entries.sumOf { it.potential } / entries.size
    }

    override suspend fun r10(): Double {
        val entries = this.r10Entries()
        if (entries.isEmpty()) return 0.0
        val potentialSum = entries.sumOf { it.potential() ?: 0.0 }
        return potentialSum / entries.size
    }

    override suspend fun potential(): Double {
        return b30() * 0.75 + r10() * 0.25
    }
}
