package xyz.sevive.arcaeaoffline.core.database.repositories

import android.util.Log
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transform
import xyz.sevive.arcaeaoffline.core.database.daos.ChartDao
import xyz.sevive.arcaeaoffline.core.database.daos.PlayResultBestDao
import xyz.sevive.arcaeaoffline.core.database.daos.PlayResultDao
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResultBestWithChart
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResultWithChart
import kotlin.system.measureTimeMillis

interface RelationshipsRepository {
    fun playResultsWithCharts(): Flow<List<PlayResultWithChart>?>
    fun playResultsBestWithCharts(limit: Int = 40): Flow<List<PlayResultBestWithChart>>
}

class RelationshipsRepositoryImpl(
    private val playResultDao: PlayResultDao,
    private val playResultBestDao: PlayResultBestDao,
    private val chartDao: ChartDao,
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
        return playResultDao.findAll().transform { list ->
            emit(null)

            val span = measureTimeMillis {
                val result = list.map {
                    PlayResultWithChart(
                        playResult = it,
                        chart = chartDao.find(it.songId, it.ratingClass).firstOrNull()
                    )
                }
                emit(result)
            }
            Log.d(LOG_TAG, "mapping play results with charts took ${span}ms")
        }
    }

    @Transaction
    override fun playResultsBestWithCharts(limit: Int): Flow<List<PlayResultBestWithChart>> {
        return playResultBestDao.orderDescWithLimit(limit).map { list ->
            list.map {
                PlayResultBestWithChart(
                    playResultBest = it,
                    chart = chartDao.find(it.songId, it.ratingClass).firstOrNull()
                )
            }
        }
    }
}
