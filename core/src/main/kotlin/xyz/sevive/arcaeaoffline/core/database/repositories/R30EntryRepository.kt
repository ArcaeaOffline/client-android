package xyz.sevive.arcaeaoffline.core.database.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import org.threeten.bp.Instant
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultClearType
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultModifier
import xyz.sevive.arcaeaoffline.core.database.daos.R30EntryDao
import xyz.sevive.arcaeaoffline.core.database.entities.ChartInfo
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.core.database.entities.Property
import xyz.sevive.arcaeaoffline.core.database.entities.R30Entry
import xyz.sevive.arcaeaoffline.core.database.entities.R30EntryAndPlayResult
import xyz.sevive.arcaeaoffline.core.database.entities.potential
import java.util.UUID

interface R30EntryRepository {
    val updating: StateFlow<Boolean>
    val updateProgress: StateFlow<Pair<Int, Int>>

    suspend fun r10(): Flow<Double?>

    suspend fun findAll(): Flow<List<R30EntryAndPlayResult>>
    suspend fun insertAll(vararg items: R30Entry)
    suspend fun deleteAll(vararg items: R30Entry)
    suspend fun emptyTable()

    suspend fun update(playResult: PlayResult)
    suspend fun update(playResults: List<PlayResult>)

    suspend fun lastUpdatedPlayResult(): PlayResult?

    suspend fun requestUpdate()
    suspend fun requestRebuild()
}

fun R30Entry.Companion.fromPlayResult(playResult: PlayResult): R30Entry {
    return R30Entry(uuid = playResult.uuid)
}

fun R30EntryAndPlayResult.Companion.fromPlayResult(playResult: PlayResult): R30EntryAndPlayResult {
    return R30EntryAndPlayResult(
        r30Entry = R30Entry.fromPlayResult(playResult),
        playResult = playResult,
    )
}

class R30EntryRepositoryImpl(
    private val r30EntryDao: R30EntryDao,
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

    override suspend fun r10(): Flow<Double?> = flow {
        val r30List = findAll().firstOrNull()
        if (r30List.isNullOrEmpty()) {
            emit(null)
            return@flow
        }
        val potentialList = r30List.mapNotNull {
            val chartInfo =
                chartInfoRepo.find(it.playResult.songId, it.playResult.ratingClass).firstOrNull()

            if (chartInfo != null) it.playResult.potential(chartInfo)
            else null
        }

        emit(potentialList.sortedByDescending { it }.take(10).average())
    }

    override suspend fun findAll(): Flow<List<R30EntryAndPlayResult>> = r30EntryDao.findAll()
    override suspend fun insertAll(vararg items: R30Entry) = r30EntryDao.insertAll(*items)
    override suspend fun deleteAll(vararg items: R30Entry) = r30EntryDao.deleteAll(*items)
    override suspend fun emptyTable() = r30EntryDao.emptyTable()

    /**
     * Return the play result with the lowest potential in the [r30EntriesWithChartInfo].
     */
    private fun minPotentialR30Entry(
        r30EntriesWithChartInfo: Map<R30EntryAndPlayResult, ChartInfo>
    ): Map.Entry<R30EntryAndPlayResult, ChartInfo> {
        return r30EntriesWithChartInfo.minBy { it.key.playResult.potential(it.value) }
    }

    /**
     * Update the R30 entries under the "conditional" circumstance.
     *
     * This will replace the lowest potential entry with the new play result.
     *
     * @param playResult The play result to be inserted
     * @param oldR30EntriesWithChartInfo Old R30 entries
     * @return The new R30 entries
     */
    private fun updateR30ByPotential(
        playResult: PlayResult, oldR30EntriesWithChartInfo: Map<R30EntryAndPlayResult, ChartInfo>
    ): List<R30EntryAndPlayResult> {
        val minPotentialEntry = minPotentialR30Entry(oldR30EntriesWithChartInfo)

        val newR30Entries = oldR30EntriesWithChartInfo.keys.toMutableList()
        newR30Entries.remove(minPotentialEntry.key)
        newR30Entries.add(R30EntryAndPlayResult.fromPlayResult(playResult))
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
    private fun updateR30ByDate(
        playResult: PlayResult, oldR30Entries: List<R30EntryAndPlayResult>
    ): List<R30EntryAndPlayResult> {
        val oldestR30Entry = oldR30Entries.minBy { it.playResult.date!!.toEpochMilli() }

        val newR30Entries = oldR30Entries.toMutableList()
        newR30Entries.remove(oldestR30Entry)
        newR30Entries.add(R30EntryAndPlayResult.fromPlayResult(playResult))
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
        playResult: PlayResult, oldR30Entries: List<R30EntryAndPlayResult>
    ): List<R30EntryAndPlayResult> {
        if (oldR30Entries.size < 30) {
            val mutableR30Entries = oldR30Entries.toMutableList()
            mutableR30Entries.add(R30EntryAndPlayResult.fromPlayResult(playResult))
            return mutableR30Entries
        }

        val newR30Entries = if (playResultMatchesThisWeirdSpecialCondition(playResult)) {
            // now check if the play result potential is bigger than the lowest potential r30 entry
            // if any chart info is missing, return the old r30 entries directly
            val playResultChartInfo =
                chartInfoRepo.find(playResult.songId, playResult.ratingClass).firstOrNull()
                    ?: return oldR30Entries
            val oldR30EntriesWithChartInfo = oldR30Entries.associateWith {
                chartInfoRepo.find(it.playResult.songId, it.playResult.ratingClass).firstOrNull()
                    ?: return oldR30Entries
            }

            val minPotentialEntry = minPotentialR30Entry(oldR30EntriesWithChartInfo)
            val minPotentialEntryPotential =
                minPotentialEntry.key.playResult.potential(minPotentialEntry.value)
            val playResultPotential = playResult.potential(playResultChartInfo)

            if (playResultPotential < minPotentialEntryPotential) return oldR30Entries

            updateR30ByPotential(playResult, oldR30EntriesWithChartInfo)
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
        propertyRepo.upsert(
            Property(
                Property.KEY_R30_LAST_UPDATED_AT, Instant.now().toEpochMilli().toString()
            )
        )
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
                return
            }

            var r30EntriesWithPlayResults = findAll().firstOrNull() ?: listOf()

            val playResultSorted = playResults.filter { it.date != null }.sortedBy { it.date }
            val total = playResultSorted.size

            playResultSorted.forEachIndexed { i, it ->
                _updateProgress.value = i to total
                r30EntriesWithPlayResults = updateR30Entries(it, r30EntriesWithPlayResults)
            }

            emptyTable()
            insertAll(*r30EntriesWithPlayResults.map { it.r30Entry }.toTypedArray())

            propertyRepo.upsert(
                Property(
                    Property.KEY_R30_LAST_UPDATED_AT, Instant.now().toEpochMilli().toString()
                )
            )
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
        val lastUpdatedPlayResultUUIDProperty =
            propertyRepo.find(Property.KEY_R30_LAST_UPDATED_PLAY_RESULT_UUID).firstOrNull()
                ?: return null
        val uuid = lastUpdatedPlayResultUUIDProperty.value

        return playResultRepo.findByUUID(UUID.fromString(uuid)).firstOrNull()
    }

    override suspend fun requestUpdate() {
        val lastUpdatedPlayResult = this.lastUpdatedPlayResult()

        if (lastUpdatedPlayResult?.date != null) {
            update(
                playResultRepo.findLaterThan(lastUpdatedPlayResult.date).firstOrNull()
                    ?: emptyList()
            )
        } else {
            requestRebuild()
        }
    }

    override suspend fun requestRebuild() {
        emptyTable()
        update(playResultRepo.findAll().firstOrNull() ?: emptyList())
    }
}
