package xyz.sevive.arcaeaoffline.jobs

import android.content.Context
import android.util.Log
import androidx.room.withTransaction
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import io.sentry.Sentry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.threeten.bp.Instant
import xyz.sevive.arcaeaoffline.ArcaeaOfflineApplication
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultClearType
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultModifier
import xyz.sevive.arcaeaoffline.core.database.ArcaeaOfflineDatabase
import xyz.sevive.arcaeaoffline.core.database.entities.ChartInfo
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.core.database.entities.potential
import xyz.sevive.arcaeaoffline.core.database.repositories.R30EntryCombined


private fun PlayResult.matchesR30Condition(): Boolean {
    // score >= EX
    if (score >= 9800000) return true
    // is hard lost
    if (clearType == ArcaeaPlayResultClearType.TRACK_LOST && modifier == ArcaeaPlayResultModifier.HARD) return true

    return false
}

private fun List<R30EntryCombined>.minPotentialItem(): R30EntryCombined? {
    return this.minByOrNull { entry ->
        entry.chartInfo?.let { entry.playResult.potential(it) } ?: Double.MAX_VALUE
    }
}

class R30UpdateJob(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    companion object {
        private const val LOG_TAG = "R30UpdateJob"
        const val WORK_NAME = "R30UpdateJob"

        const val DATA_RUN_MODE = "run_mode"

        const val KEY_PROGRESS = "progress"
        const val KEY_PROGRESS_TOTAL = "progress_total"
    }

    enum class RunMode(val value: Int) { NORMAL(0), REBUILD(1) }

    private data class WorkOptions(val runMode: RunMode)

    private fun getWorkOptions(): WorkOptions {
        return WorkOptions(
            runMode = RunMode.entries.firstOrNull {
                it.value == inputData.getInt(DATA_RUN_MODE, 0)
            } ?: RunMode.NORMAL,
        )
    }

    private val repoContainer =
        (applicationContext as ArcaeaOfflineApplication).arcaeaOfflineDatabaseRepositoryContainer
    private val r30EntryRepo = repoContainer.r30EntryRepo
    private val propertyRepo = repoContainer.propertyRepo
    private val playResultRepo = repoContainer.playResultRepo
    private val chartInfoRepo = repoContainer.chartInfoRepo

    private val progress = MutableStateFlow(0)
    private val progressTotal = MutableStateFlow(-1)
    private val progressListenScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override suspend fun doWork(): Result {
        progressListenScope.launch {
            combine(progress, progressTotal) { p, t -> p to t }.collectLatest {
                setProgress(
                    workDataOf(KEY_PROGRESS to it.first, KEY_PROGRESS_TOTAL to it.second)
                )
            }
        }

        val workOptions = getWorkOptions()

        try {
            val r30LastUpdatedAt = propertyRepo.r30LastUpdatedAt()

            var r30EntryCombinedList = when (workOptions.runMode) {
                RunMode.REBUILD -> emptyList()
                else -> r30EntryRepo.findAllCombined().firstOrNull() ?: emptyList()
            }

            val playResults = when (workOptions.runMode) {
                RunMode.REBUILD -> playResultRepo.findAll().firstOrNull()
                else -> r30LastUpdatedAt?.let { playResultRepo.findLaterThan(it).firstOrNull() }
            } ?: emptyList()
            val newPlayResults = playResults.filter { it.date != null }.sortedBy { it.date }

            progressTotal.value = newPlayResults.size
            Log.d(LOG_TAG, "Updating r30 list with ${newPlayResults.size} new play results")
            newPlayResults.forEach {
                r30EntryCombinedList = updateR30List(it, r30EntryCombinedList)
                progress.value += 1
            }

            ArcaeaOfflineDatabase.getDatabase(applicationContext).withTransaction {
                r30EntryRepo.deleteAll()
                r30EntryRepo.insertBatch(*r30EntryCombinedList.map { it.entry }.toTypedArray())

                propertyRepo.setR30LastUpdatedAt(Instant.now())
            }

            return Result.success()
        } catch (e: Throwable) {
            Log.e(LOG_TAG, "Error updating r30", e)
            Sentry.captureException(e)
            return Result.failure()
        }
    }

    /**
     * Wrapper of [updateR30ListByDate] and [updateR30ListByPotential] that automatically
     * choose one of them depending on the [playResult]'s state.
     */
    private suspend fun updateR30List(
        playResult: PlayResult, oldR30List: List<R30EntryCombined>
    ): List<R30EntryCombined> {
        if (oldR30List.size < 30) {
            val mutableR30List = oldR30List.toMutableList()
            mutableR30List.add(R30EntryCombined.build(playResult, chartInfoRepo))
            return mutableR30List
        }

        val newR30Entries = if (playResult.matchesR30Condition()) {
            // now check if the play result potential is higher than the lowest potential r30 entry
            // if any chart info is missing, return the old r30 entries directly
            val chartInfo = chartInfoRepo.find(playResult).firstOrNull() ?: return oldR30List
            updateR30ListByPotential(playResult, chartInfo, oldR30List)
        } else {
            // otherwise, just update the entries by date
            updateR30ListByDate(playResult, oldR30List)
        }

        // ensure the new r30 should have at least 10 unique charts
        // otherwise keep the entries unmodified
        val uniqueChartsCount =
            newR30Entries.distinctBy { "${it.playResult.songId}|${it.playResult.ratingClass.value}" }
                .count()
        return if (uniqueChartsCount < 10) oldR30List
        else newR30Entries
    }

    /**
     * Update the R30 entries under the "conditional" circumstance.
     *
     * This will replace the lowest potential entry with the new play result.
     *
     * @param playResult The play result to be inserted
     * @param chartInfo The [ChartInfo] of [playResult]
     * @param oldR30List Old R30 entries
     * @return The new R30 entries
     */
    private fun updateR30ListByPotential(
        playResult: PlayResult, chartInfo: ChartInfo, oldR30List: List<R30EntryCombined>
    ): List<R30EntryCombined> {
        // try getting the min potential item in old list
        // otherwise leave the old list untouched
        val minPotentialEntry = oldR30List.minPotentialItem() ?: return oldR30List
        val minPotentialEntryPotential = minPotentialEntry.potential() ?: return oldR30List

        if (playResult.potential(chartInfo) < minPotentialEntryPotential) return oldR30List

        val newR30Entries = oldR30List.toMutableList()
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
     * @param oldR30List Old R30 entries
     * @return The new R30 entries
     */
    private suspend fun updateR30ListByDate(
        playResult: PlayResult, oldR30List: List<R30EntryCombined>
    ): List<R30EntryCombined> {
        val oldestR30Entry = oldR30List.minByOrNull {
            it.playResult.date?.toEpochMilli() ?: Long.MAX_VALUE
        } ?: return oldR30List

        val newR30Entries = oldR30List.toMutableList()
        newR30Entries.remove(oldestR30Entry)
        newR30Entries.add(R30EntryCombined.build(playResult, chartInfoRepo))
        return newR30Entries
    }
}
