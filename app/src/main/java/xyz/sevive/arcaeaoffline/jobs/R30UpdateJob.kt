package xyz.sevive.arcaeaoffline.jobs

import android.content.Context
import androidx.room.immediateTransaction
import androidx.room.useWriterConnection
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import co.touchlab.kermit.Logger
import io.sentry.Sentry
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import xyz.sevive.arcaeaoffline.core.Progress
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultClearType
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultModifier
import xyz.sevive.arcaeaoffline.core.database.ArcaeaOfflineDatabase
import xyz.sevive.arcaeaoffline.core.database.entities.ChartInfo
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.core.database.entities.playRating
import xyz.sevive.arcaeaoffline.core.database.repositories.ChartInfoRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.PlayResultRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.PropertyRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.R30EntryCombined
import xyz.sevive.arcaeaoffline.core.database.repositories.R30EntryRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.SongRepository
import xyz.sevive.arcaeaoffline.helpers.toWorkData
import kotlin.time.Clock

private fun PlayResult.matchesR30Condition(): Boolean {
    // score >= EX
    if (score >= 9800000) return true
    // is hard lost
    if (clearType == ArcaeaPlayResultClearType.TRACK_LOST && modifier == ArcaeaPlayResultModifier.HARD) return true

    return false
}

private fun List<R30EntryCombined>.minPlayRatingItem(): R30EntryCombined? =
    this.minByOrNull { entry ->
        entry.chartInfo?.let { entry.playResult.playRating(it) } ?: Double.MAX_VALUE
    }

class R30UpdateJob(
    context: Context,
    params: WorkerParameters,
    private val db: ArcaeaOfflineDatabase,
    private val r30EntryRepo: R30EntryRepository,
    private val propertyRepo: PropertyRepository,
    private val songRepo: SongRepository,
    private val playResultRepo: PlayResultRepository,
    private val chartInfoRepo: ChartInfoRepository,
) : CoroutineWorker(context, params) {
    companion object {
        private const val LOG_TAG = "R30UpdateJob"
        const val WORK_NAME = "R30UpdateJob"

        const val DATA_RUN_MODE = "run_mode"
    }

    enum class RunMode(
        val value: Int,
    ) {
        NORMAL(0),
        REBUILD(1),
    }

    private data class WorkOptions(
        val runMode: RunMode,
    )

    private val logger = Logger.withTag(LOG_TAG)

    private fun getWorkOptions(): WorkOptions =
        WorkOptions(
            runMode =
                RunMode.entries.firstOrNull {
                    it.value == inputData.getInt(DATA_RUN_MODE, 0)
                } ?: RunMode.NORMAL,
        )

    private val progressFlow = MutableStateFlow(Progress.INDETERMINATE)

    override suspend fun doWork(): Result {
        val workOptions = getWorkOptions()

        try {
            return coroutineScope {
                val progressPublishJob =
                    launch {
                        progressFlow.collectLatest { setProgress(it.toWorkData()) }
                    }

                val r30LastUpdatedAt = propertyRepo.r30LastUpdatedAt()

                var r30EntryCombinedList =
                    when (workOptions.runMode) {
                        RunMode.REBUILD -> emptyList()
                        else -> r30EntryRepo.findAllCombined().firstOrNull() ?: emptyList()
                    }

                val playResults =
                    when (workOptions.runMode) {
                        RunMode.REBUILD -> playResultRepo.findAll().firstOrNull()
                        else -> r30LastUpdatedAt?.let { playResultRepo.findLaterThan(it).firstOrNull() }
                    } ?: emptyList()
                val deletedSongIds =
                    songRepo.findDeletedInGame().firstOrNull()?.map { it.id } ?: emptyList()
                val newPlayResults =
                    playResults
                        .filter { it.date != null && it.songId !in deletedSongIds }
                        .sortedBy { it.date }

                progressFlow.update { Progress(current = 0, total = newPlayResults.size) }
                logger.d { "Updating r30 list with ${newPlayResults.size} new play results" }
                newPlayResults.forEach {
                    r30EntryCombinedList = updateR30List(it, r30EntryCombinedList)
                    progressFlow.update { progress -> progress.increment() }
                }

                // Room3 possibly has a convenient extension function for this
                // see https://issuetracker.google.com/issues/416306996
                db.useWriterConnection { transactor ->
                    transactor.immediateTransaction {
                        r30EntryRepo.deleteAll()
                        r30EntryRepo.insertBatch(*r30EntryCombinedList.map { it.entry }.toTypedArray())

                        propertyRepo.setR30LastUpdatedAt(Clock.System.now())
                    }
                }

                progressPublishJob.cancelAndJoin()
                Result.success()
            }
        } catch (e: Throwable) {
            if (e is CancellationException) throw e

            logger.e(e) { "Error updating r30" }
            Sentry.captureException(e)
            return Result.failure()
        }
    }

    /**
     * Wrapper of [updateR30ListByDate] and [updateR30ListByPotential] that automatically
     * choose one of them depending on the [playResult]'s state.
     */
    private suspend fun updateR30List(
        playResult: PlayResult,
        oldR30List: List<R30EntryCombined>,
    ): List<R30EntryCombined> {
        if (oldR30List.size < 30) {
            val mutableR30List = oldR30List.toMutableList()
            mutableR30List.add(R30EntryCombined.build(playResult, chartInfoRepo))
            return mutableR30List
        }

        val newR30Entries =
            if (playResult.matchesR30Condition()) {
                // now check if the play result play rating is higher than the lowest play rating r30 entry
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
            newR30Entries
                .distinctBy { "${it.playResult.songId}|${it.playResult.ratingClass.value}" }
                .count()
        return if (uniqueChartsCount < 10) {
            oldR30List
        } else {
            newR30Entries
        }
    }

    /**
     * Update the R30 entries under the "conditional" circumstance.
     *
     * This will replace the lowest play rating entry with the new play result.
     *
     * @param playResult The play result to be inserted
     * @param chartInfo The [ChartInfo] of [playResult]
     * @param oldR30List Old R30 entries
     * @return The new R30 entries
     */
    private fun updateR30ListByPotential(
        playResult: PlayResult,
        chartInfo: ChartInfo,
        oldR30List: List<R30EntryCombined>,
    ): List<R30EntryCombined> {
        // try getting the min play rating item in old list
        // otherwise leave the old list untouched
        val minRatingEntry = oldR30List.minPlayRatingItem() ?: return oldR30List
        val minRatingEntryRating = minRatingEntry.playRating() ?: return oldR30List

        if (playResult.playRating(chartInfo) < minRatingEntryRating) return oldR30List

        val newR30Entries = oldR30List.toMutableList()
        newR30Entries.remove(minRatingEntry)
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
        playResult: PlayResult,
        oldR30List: List<R30EntryCombined>,
    ): List<R30EntryCombined> {
        val oldestR30Entry =
            oldR30List.minByOrNull {
                it.playResult.date?.toEpochMilliseconds() ?: Long.MAX_VALUE
            } ?: return oldR30List

        val newR30Entries = oldR30List.toMutableList()
        newR30Entries.remove(oldestR30Entry)
        newR30Entries.add(R30EntryCombined.build(playResult, chartInfoRepo))
        return newR30Entries
    }
}
