package xyz.sevive.arcaeaoffline.core.database.repositories

import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import xyz.sevive.arcaeaoffline.core.database.daos.ChartDao
import xyz.sevive.arcaeaoffline.core.database.daos.PlayResultBestDao
import xyz.sevive.arcaeaoffline.core.database.daos.PlayResultDao
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResultBestWithChart
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResultWithChart

interface RelationshipsRepository {
    fun playResultsWithCharts(): Flow<List<PlayResultWithChart>>
    fun playResultsBestWithCharts(limit: Int = 40): Flow<List<PlayResultBestWithChart>>
}

class RelationshipsRepositoryImpl(
    private val playResultDao: PlayResultDao,
    private val playResultBestDao: PlayResultBestDao,
    private val chartDao: ChartDao,
) : RelationshipsRepository {
    @Transaction
    override fun playResultsWithCharts(): Flow<List<PlayResultWithChart>> {
        return playResultDao.findAll().map {
            it.map {
                PlayResultWithChart(
                    playResult = it,
                    chart = chartDao.find(it.songId, it.ratingClass).firstOrNull()
                )
            }
        }
    }

    @Transaction
    override fun playResultsBestWithCharts(limit: Int): Flow<List<PlayResultBestWithChart>> {
        return playResultBestDao.orderDescWithLimit(limit).map {
            it.map {
                PlayResultBestWithChart(
                    playResultBest = it,
                    chart = chartDao.find(it.songId, it.ratingClass).firstOrNull()
                )
            }
        }
    }
}
