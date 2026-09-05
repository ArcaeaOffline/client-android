package xyz.sevive.arcaeaoffline.core.database.repositories

import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import xyz.sevive.arcaeaoffline.core.database.daos.ChartDao
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResultBestWithChart

interface RelationshipsRepository {
    fun playResultsBestWithCharts(limit: Int = 40): Flow<List<PlayResultBestWithChart>>
}

class RelationshipsRepositoryImpl(
    private val playResultBestRepository: PlayResultBestRepository,
    private val chartDao: ChartDao,
) : RelationshipsRepository {
    companion object {
        const val LOG_TAG = "RelationshipsRepoImpl"
    }

    @Transaction
    override fun playResultsBestWithCharts(limit: Int): Flow<List<PlayResultBestWithChart>> =
        playResultBestRepository.orderDescWithLimit(limit).map { list ->
            list.map {
                val chart = chartDao.find(it.songId, it.ratingClass).firstOrNull()
                PlayResultBestWithChart(playResultBest = it, chart = chart)
            }
        }
}
