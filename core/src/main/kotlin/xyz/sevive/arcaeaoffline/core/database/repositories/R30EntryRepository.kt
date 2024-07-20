package xyz.sevive.arcaeaoffline.core.database.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import org.threeten.bp.Instant
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultClearType
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultModifier
import xyz.sevive.arcaeaoffline.core.database.daos.R30EntryDao
import xyz.sevive.arcaeaoffline.core.database.entities.ChartInfo
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.core.database.entities.R30Entry
import xyz.sevive.arcaeaoffline.core.database.entities.potential


data class R30EntryCombined(
    val entry: R30Entry,
    val playResult: PlayResult,
    val chartInfo: ChartInfo?,
) {
    fun potential(): Double? {
        return if (chartInfo == null) null else playResult.potential(chartInfo)
    }

    companion object {
        fun build(playResult: PlayResult, chartInfo: ChartInfo?): R30EntryCombined {
            return R30EntryCombined(
                entry = R30Entry(uuid = playResult.uuid),
                playResult = playResult,
                chartInfo = chartInfo,
            )
        }

        suspend fun build(
            playResult: PlayResult, chartInfoRepository: ChartInfoRepository
        ): R30EntryCombined {
            val chartInfo = chartInfoRepository.find(playResult).firstOrNull()
            return this.build(playResult, chartInfo)
        }
    }
}

interface R30EntryRepository {
    val updating: StateFlow<Boolean>
    val updateProgress: StateFlow<Pair<Int, Int>>

    fun findAll(): Flow<List<R30Entry>>
    fun findAllCombined(): Flow<List<R30EntryCombined>>
    suspend fun insertAll(vararg items: R30Entry)
    suspend fun deleteAll(vararg items: R30Entry)
    suspend fun emptyTable()

    suspend fun update(playResult: PlayResult)
    suspend fun update(playResults: List<PlayResult>)

    suspend fun lastUpdatedPlayResult(): PlayResult?

    suspend fun requestUpdate()
    suspend fun requestRebuild()
}

class R30EntryRepositoryImpl(
    private val dao: R30EntryDao,
    private val playResultRepo: PlayResultRepository,
    private val chartInfoRepo: ChartInfoRepository,
    private val propertyRepo: PropertyRepository,
) : R30EntryRepository {
    companion object {
        const val LOG_TAG = "R30EntryRepoImpl"
    }

    private val _updating = MutableStateFlow(false)
    override val updating = _updating.asStateFlow()

    private val _updateProgress = MutableStateFlow(0 to -1)
    override val updateProgress: StateFlow<Pair<Int, Int>> = _updateProgress.asStateFlow()

    override fun findAll(): Flow<List<R30Entry>> = dao.findAll()
    override suspend fun insertAll(vararg items: R30Entry) = dao.insertAll(*items)
    override suspend fun deleteAll(vararg items: R30Entry) = dao.deleteAll(*items)
    override suspend fun emptyTable() = dao.emptyTable()

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

    /**
     * Return the play result with the lowest potential in the [r30EntriesCombined].
     */
    private fun minPotentialR30Entry(r30EntriesCombined: List<R30EntryCombined>): R30EntryCombined? {
        return r30EntriesCombined.minByOrNull {
            if (it.chartInfo == null) Double.MAX_VALUE
            else it.playResult.potential(it.chartInfo)
        }
    }

    /**
     * Update the R30 entries under the "conditional" circumstance.
     *
     * This will replace the lowest potential entry with the new play result.
     *
     * @param playResult The play result to be inserted
     * @param chartInfo The [ChartInfo] of [playResult]
     * @param oldR30EntriesCombined Old R30 entries
     * @return The new R30 entries
     */
    private fun updateR30ByPotential(
        playResult: PlayResult, chartInfo: ChartInfo, oldR30EntriesCombined: List<R30EntryCombined>
    ): List<R30EntryCombined> {
        val minPotentialEntry =
            minPotentialR30Entry(oldR30EntriesCombined) ?: return oldR30EntriesCombined
        val minPotentialEntryPotential =
            minPotentialEntry.potential() ?: return oldR30EntriesCombined
        if (playResult.potential(chartInfo) < minPotentialEntryPotential) return oldR30EntriesCombined

        val newR30Entries = oldR30EntriesCombined.toMutableList()
        newR30Entries.remove(minPotentialEntry)
        newR30Entries.add(R30EntryCombined.build(playResult, chartInfo))
        return newR30Entries
    }

    /**
     * Update the R30 entries as usual.
     *
     * This will replace the oldest entry with the new play result.
     *
     * @param playResult The play result to be inserted
     * @param oldR30Entries Old R30 entries
     * @return The new R30 entries
     */
    private suspend fun updateR30ByDate(
        playResult: PlayResult, oldR30Entries: List<R30EntryCombined>
    ): List<R30EntryCombined> {
        val oldestR30Entry = oldR30Entries.minByOrNull {
            if (it.playResult.date == null) Long.MAX_VALUE
            else it.playResult.date.toEpochMilli()
        } ?: return oldR30Entries

        val newR30Entries = oldR30Entries.toMutableList()
        newR30Entries.remove(oldestR30Entry)
        newR30Entries.add(R30EntryCombined.build(playResult, chartInfoRepo))
        return newR30Entries
    }

    /**
     * Whether the [playResult] matches the "conditional" circumstance.
     */
    private fun playResultMatchesThisWeirdSpecialCondition(playResult: PlayResult): Boolean {
        // score >= EX
        if (playResult.score >= 9800000) return true
        // is hard lost
        if (playResult.clearType == ArcaeaPlayResultClearType.TRACK_LOST && playResult.modifier == ArcaeaPlayResultModifier.HARD) return true

        return false
    }

    private suspend fun updateR30Entries(
        playResult: PlayResult, oldR30Entries: List<R30EntryCombined>
    ): List<R30EntryCombined> {
        if (oldR30Entries.size < 30) {
            val mutableR30Entries = oldR30Entries.toMutableList()
            mutableR30Entries.add(R30EntryCombined.build(playResult, chartInfoRepo))
            return mutableR30Entries
        }

        val newR30Entries = if (playResultMatchesThisWeirdSpecialCondition(playResult)) {
            // now check if the play result potential is bigger than the lowest potential r30 entry
            // if any chart info is missing, return the old r30 entries directly
            val playResultChartInfo =
                chartInfoRepo.find(playResult).firstOrNull() ?: return oldR30Entries

            updateR30ByPotential(playResult, playResultChartInfo, oldR30Entries)
        } else {
            // otherwise, just update the entries by date
            updateR30ByDate(playResult, oldR30Entries)
        }

        // ensure the new r30 should have at least 10 unique charts
        // otherwise keep the entries unmodified
        val uniqueChartsCount =
            newR30Entries.distinctBy { "${it.playResult.songId}|${it.playResult.ratingClass.value}" }
                .count()
        return if (uniqueChartsCount < 10) oldR30Entries
        else newR30Entries
    }

    private suspend fun updateR30LastUpdatedAt() {
        propertyRepo.setR30LastUpdatedAt(Instant.now())
    }

    /**
     * Update the R30 entries, then write the new entries into database.
     * This function will also handle the `updating` and `updateProgress` states.
     */
    private suspend fun iDunnoHowToNameThisAnywayUpdateIntoDatabase(playResults: List<PlayResult>) {
        _updating.value = true

        try {
            if (playResults.isEmpty()) {
                updateR30LastUpdatedAt()
                _updating.value = false
                return
            }

            var r30EntriesCombined = this.findAllCombined().firstOrNull() ?: listOf()

            val playResultSorted = playResults.filter { it.date != null }.sortedBy { it.date }
            val total = playResultSorted.size

            playResultSorted.forEachIndexed { i, it ->
                _updateProgress.value = i to total
                r30EntriesCombined = updateR30Entries(it, r30EntriesCombined)
            }

            emptyTable()
            insertAll(*r30EntriesCombined.map { it.entry }.toTypedArray())

            updateR30LastUpdatedAt()
        } finally {
            _updating.value = false
            _updateProgress.value = 0 to -1
        }
    }

    override suspend fun update(playResult: PlayResult) {
        iDunnoHowToNameThisAnywayUpdateIntoDatabase(listOf(playResult))
    }

    override suspend fun update(playResults: List<PlayResult>) {
        iDunnoHowToNameThisAnywayUpdateIntoDatabase(playResults)
    }

    override suspend fun lastUpdatedPlayResult(): PlayResult? {
        val uuid = propertyRepo.r30LastUpdatedPlayResultUUID() ?: return null
        return playResultRepo.findByUUID(uuid).firstOrNull()
    }

    override suspend fun requestUpdate() {
        val lastPlayResult = this.lastUpdatedPlayResult()

        if (lastPlayResult?.date != null) {
            val newPlayResults =
                playResultRepo.findLaterThan(lastPlayResult.date).firstOrNull() ?: emptyList()
            update(newPlayResults)
        } else {
            requestRebuild()
        }
    }

    override suspend fun requestRebuild() {
        emptyTable()
        update(playResultRepo.findAll().firstOrNull() ?: emptyList())
    }
}
