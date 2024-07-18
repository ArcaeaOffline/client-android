package xyz.sevive.arcaeaoffline.core.database.repositories

import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transform
import xyz.sevive.arcaeaoffline.core.database.daos.RelationshipsDao
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResultBestWithChart
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResultWithChart

interface RelationshipsRepository {
    fun playResultsWithCharts(): Flow<List<PlayResultWithChart>?>
    fun playResultsBestWithCharts(limit: Int = 40): Flow<List<PlayResultBestWithChart>>
}

class RelationshipsRepositoryImpl(
    private val relationshipsDao: RelationshipsDao,
) : RelationshipsRepository {
    companion object {
        const val LOG_TAG = "RelationshipsRepoImpl"
    }

    /**
     * List of [PlayResultWithChart] objects in database.
     *
     * The `null` value of the flow indicates that a new list is on the way,
     * you may change the UI into a loading state, then display the results
     * when a new list is emitted. Use [kotlinx.coroutines.flow.filterNotNull]
     * if you only need the non-null values.
     */
    @Transaction
    override fun playResultsWithCharts(): Flow<List<PlayResultWithChart>?> {
        return relationshipsDao.playResultsWithCharts().transform { list ->
            emit(null)

            val result = list.map { PlayResultWithChart(playResult = it.key, chart = it.value) }
            emit(result)
        }
    }

    @Transaction
    override fun playResultsBestWithCharts(limit: Int): Flow<List<PlayResultBestWithChart>> {
        return relationshipsDao.playResultsBestWithCharts(limit).map { list ->
            list.map { PlayResultBestWithChart(playResultBest = it.key, chart = it.value) }
        }
    }
}
