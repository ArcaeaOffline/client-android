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
    }

    private val logger = Logger.withTag(LOG_TAG)

    private val progressFlow = MutableStateFlow(Progress.INDETERMINATE)

    override suspend fun doWork(): Result {
        try {
            return coroutineScope {
                val progressPublishJob =
                    launch {
                        progressFlow.collectLatest { setProgress(it.toWorkData()) }
                    }

                val deletedSongIds = songRepo.findDeletedInGame().firstOrNull()?.map { it.id } ?: emptyList()
                val playResults =
                    (playResultRepo.findAll().firstOrNull() ?: emptyList())
                        .filter { it.date != null && it.songId !in deletedSongIds }
                        .sortedWith(compareBy({ it.date }, { it.id }))

                progressFlow.update { Progress(current = 0, total = playResults.size) }
                logger.d { "Rebuilding r30 list from ${playResults.size} play results" }
                val r30EntryCombinedList =
                    r30QueueUpdater.rebuild(
                        plays = playResults,
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
