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
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import xyz.sevive.arcaeaoffline.core.Progress
import xyz.sevive.arcaeaoffline.core.database.ArcaeaOfflineDatabase
import xyz.sevive.arcaeaoffline.core.database.r30.ChartKey
import xyz.sevive.arcaeaoffline.core.database.r30.R30QueueUpdater
import xyz.sevive.arcaeaoffline.core.database.repositories.ChartInfoRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.PlayResultRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.PropertyRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.R30EntryRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.SongRepository
import xyz.sevive.arcaeaoffline.helpers.toWorkData
import kotlin.time.Clock

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
    private val r30QueueUpdater = R30QueueUpdater { chartInfoRepo.find(it).firstOrNull() }

    companion object {
        private const val LOG_TAG = "R30UpdateJob"
        const val WORK_NAME = "R30UpdateJob"

        const val DATA_RUN_MODE = "run_mode"
    }

    private val logger = Logger.withTag(LOG_TAG)

    enum class RunMode(
        val value: Int,
    ) {
        NORMAL(0),
        REBUILD(1), ;

        companion object {
            fun fromInt(value: Int) = entries.firstOrNull { it.value == value }
        }
    }

    private data class WorkOptions(
        val runMode: RunMode,
    )

    private fun parseRunMode(): RunMode {
        val runModeInput = inputData.getInt(DATA_RUN_MODE, 0)
        val result = RunMode.fromInt(runModeInput)
        if (result == null) logger.w { "Invalid RunMode $runModeInput, falling back to ${RunMode.NORMAL}" }
        return result ?: RunMode.NORMAL
    }

    private fun getWorkOptions(): WorkOptions =
        WorkOptions(
            runMode = parseRunMode(),
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
                val cutoff = r30LastUpdatedAt.takeUnless { workOptions.runMode == RunMode.REBUILD }

                var r30EntryCombinedList =
                    if (cutoff == null) {
                        emptyList()
                    } else {
                        r30EntryRepo.findAllCombined().firstOrNull() ?: emptyList()
                    }

                // Records of the plays outside the batch, so an incremental run decides on a
                // new chart record the same way a rebuild from the whole history would.
                val previousBestScores =
                    cutoff
                        ?.let { playResultRepo.bestScoresUntil(it) }
                        ?.associate { ChartKey(it.songId, it.ratingClass) to it.score }
                        .orEmpty()

                val playResults =
                    if (cutoff == null) {
                        playResultRepo.findAll().firstOrNull() ?: emptyList()
                    } else {
                        playResultRepo.findLaterThan(cutoff).firstOrNull() ?: emptyList()
                    }
                val deletedSongIds = songRepo.findDeletedInGame().firstOrNull()?.map { it.id } ?: emptyList()
                val newPlayResults =
                    playResults
                        .filter { it.date != null && it.songId !in deletedSongIds }
                        .sortedWith(compareBy({ it.date }, { it.id }))

                progressFlow.update { Progress(current = 0, total = newPlayResults.size) }
                logger.d { "Updating r30 list with ${newPlayResults.size} new play results" }
                r30EntryCombinedList =
                    r30QueueUpdater.replay(
                        plays = newPlayResults,
                        entries = r30EntryCombinedList,
                        previousBestScores = previousBestScores,
                        onPlay = {
                            ensureActive()
                            progressFlow.update { progress -> progress.increment() }
                        },
                    )

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
        } catch (e: Exception) {
            if (e is CancellationException) throw e

            logger.e(e) { "Error updating r30" }
            Sentry.captureException(e)
            return Result.failure()
        }
    }
}
