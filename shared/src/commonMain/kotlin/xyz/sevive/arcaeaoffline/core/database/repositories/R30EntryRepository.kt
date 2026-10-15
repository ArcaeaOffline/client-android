package xyz.sevive.arcaeaoffline.core.database.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import xyz.sevive.arcaeaoffline.core.database.daos.R30EntryDao
import xyz.sevive.arcaeaoffline.core.database.entities.ChartInfo
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.core.database.entities.R30Entry
import xyz.sevive.arcaeaoffline.core.database.entities.playRating

data class R30EntryCombined(
    val entry: R30Entry,
    val playResult: PlayResult,
    val chartInfo: ChartInfo?,
) {
    fun playRating(): Double? = if (chartInfo == null) null else playResult.playRating(chartInfo)

    companion object {
        fun build(
            playResult: PlayResult,
            chartInfo: ChartInfo?,
        ): R30EntryCombined =
            R30EntryCombined(
                entry = R30Entry(uuid = playResult.uuid),
                playResult = playResult,
                chartInfo = chartInfo,
            )

        suspend fun build(
            playResult: PlayResult,
            chartInfoRepository: ChartInfoRepository,
        ): R30EntryCombined {
            val chartInfo = chartInfoRepository.find(playResult).firstOrNull()
            return this.build(playResult, chartInfo)
        }
    }
}

interface R30EntryRepository {
    fun findAll(): Flow<List<R30Entry>>

    fun findAllCombined(): Flow<List<R30EntryCombined>>

    suspend fun insertBatch(vararg items: R30Entry)

    suspend fun deleteBatch(vararg items: R30Entry)

    suspend fun deleteAll()
}

class R30EntryRepositoryImpl(
    private val dao: R30EntryDao,
) : R30EntryRepository {
    companion object {
        const val LOG_TAG = "R30EntryRepoImpl"
    }

    override fun findAll(): Flow<List<R30Entry>> = dao.findAll()

    override suspend fun insertBatch(vararg items: R30Entry) = dao.insertBatch(*items)

    override suspend fun deleteBatch(vararg items: R30Entry) = dao.deleteBatch(*items)

    override suspend fun deleteAll() = dao.deleteAll()

    override fun findAllCombined(): Flow<List<R30EntryCombined>> {
        return combine(dao.findAllWithPlayResult(), dao.findAllWithChartInfo()) { mpr, mci ->
            val r30Entries = mpr.keys

            r30Entries.mapNotNull {
                val playResult = mpr[it] ?: return@mapNotNull null
                val chartInfo = mci[it]
                R30EntryCombined(it, playResult, chartInfo)
            }
        }
    }
}
