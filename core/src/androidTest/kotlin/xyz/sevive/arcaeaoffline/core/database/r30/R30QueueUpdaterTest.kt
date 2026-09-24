package xyz.sevive.arcaeaoffline.core.database.r30

import android.content.Context
import androidx.room.Room
import androidx.room.execSQL
import androidx.room.immediateTransaction
import androidx.room.useWriterConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.database.ArcaeaOfflineDatabase
import xyz.sevive.arcaeaoffline.core.database.repositories.ChartInfoRepositoryImpl
import xyz.sevive.arcaeaoffline.core.database.repositories.PlayResultBestRepositoryImpl
import xyz.sevive.arcaeaoffline.core.database.repositories.PlayResultCalculatedRepositoryImpl
import xyz.sevive.arcaeaoffline.core.database.repositories.PlayResultRepositoryImpl
import xyz.sevive.arcaeaoffline.core.database.repositories.PotentialRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.PotentialRepositoryImpl
import xyz.sevive.arcaeaoffline.core.database.repositories.PropertyRepositoryImpl
import xyz.sevive.arcaeaoffline.core.database.repositories.R30EntryCombined
import xyz.sevive.arcaeaoffline.core.database.repositories.R30EntryRepositoryImpl

/** The recent queue rebuilt from the play history. */
@RunWith(AndroidJUnit4::class)
class R30QueueUpdaterTest {
    private lateinit var db: ArcaeaOfflineDatabase
    private lateinit var potentialRepository: PotentialRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db =
            Room
                .inMemoryDatabaseBuilder(context, ArcaeaOfflineDatabase::class.java)
                .setDriver(BundledSQLiteDriver())
                .build()

        potentialRepository =
            PotentialRepositoryImpl(
                PlayResultBestRepositoryImpl(
                    db.playResultBestDao(),
                    PlayResultCalculatedRepositoryImpl(db.playResultDao(), db.songDao(), db.chartInfoDao()),
                ),
                R30EntryRepositoryImpl(db.r30EntryDao()),
                PropertyRepositoryImpl(db.propertyDao()),
            )

        runBlocking { seed() }
    }

    @After
    fun tearDown() {
        db.close()
    }

    private suspend fun seed() {
        val statements =
            readSeedScript()
                .lineSequence()
                .filterNot { it.trimStart().startsWith("--") }
                .joinToString("\n")
                .split(';')
                .map { it.trim() }
                .filter { it.isNotEmpty() }

        db.useWriterConnection { connection ->
            connection.immediateTransaction {
                statements.forEach { execSQL(it) }
            }
        }
    }

    private fun readSeedScript(): String {
        val stream = javaClass.classLoader?.getResourceAsStream(SEED_FILE) ?: error("Seed file not found: $SEED_FILE")
        return stream.bufferedReader().use { it.readText() }
    }

    private suspend fun replayQueue(): List<R30EntryCombined> {
        val chartInfoRepository = ChartInfoRepositoryImpl(db.chartInfoDao())
        val playResults =
            PlayResultRepositoryImpl(db.playResultDao())
                .findAll()
                .first()
                .sortedWith(compareBy({ it.date }, { it.id }))

        return R30QueueUpdater { chartInfoRepository.find(it).firstOrNull() }.replay(playResults)
    }

    private data class QueueEntry(
        val songId: String,
        val ratingClass: ArcaeaRatingClass,
        val score: Int,
        val date: Long?,
    )

    private fun List<R30EntryCombined>.toQueueEntries(): List<QueueEntry> =
        map {
            QueueEntry(
                songId = it.playResult.songId,
                ratingClass = it.playResult.ratingClass,
                score = it.playResult.score,
                date = it.playResult.date?.toEpochMilliseconds(),
            )
        }.sortedWith(compareBy({ it.songId }, { it.ratingClass }, { it.score }, { it.date }))

    @Test
    fun replayRebuildsTheQueueOfTheSave() =
        runBlocking {
            assertEquals(EXPECTED_QUEUE, replayQueue().toQueueEntries())
        }

    @Test
    fun recentTopTenCountsEachChartOnce() =
        runBlocking {
            val queue = replayQueue()

            // 18 of the 30 entries are distinct charts, so whether they are counted once or per
            // play decides the average below.
            assertEquals(18, queue.map { ChartKey(it.playResult.songId, it.playResult.ratingClass) }.distinct().size)
            db.r30EntryDao().insertBatch(*queue.map { it.entry }.toTypedArray())

            assertEquals(12.524324, potentialRepository.r10().first(), TOLERANCE)
        }

    private companion object {
        const val SEED_FILE = "xyz/sevive/arcaeaoffline/core/database/r30/r30_seed.sql"

        /**
         * Repositories sum play ratings in list order; the expected values were summed in a
         * different one, and 1e-7 stays far below what the app shows.
         */
        const val TOLERANCE = 1e-7

        val EXPECTED_QUEUE =
            listOf(
                QueueEntry("aethercrest", ArcaeaRatingClass.ETERNAL, 9757015, 1767253713219),
                QueueEntry("aethercrest", ArcaeaRatingClass.ETERNAL, 9816277, 1766662160675),
                QueueEntry("blackmind", ArcaeaRatingClass.FUTURE, 9906944, 1768549652129),
                QueueEntry("cyaegha", ArcaeaRatingClass.FUTURE, 9939116, 1768549496384),
                QueueEntry("egoeimi", ArcaeaRatingClass.FUTURE, 9960223, 1768023159302),
                QueueEntry("einherjar", ArcaeaRatingClass.BEYOND, 9920907, 1766660967276),
                QueueEntry("extradimensional", ArcaeaRatingClass.ETERNAL, 9813212, 1766661646868),
                QueueEntry("extradimensional", ArcaeaRatingClass.ETERNAL, 9819009, 1766399632532),
                QueueEntry("extradimensional", ArcaeaRatingClass.ETERNAL, 9853769, 1769502077902),
                QueueEntry("gloryroad", ArcaeaRatingClass.FUTURE, 9970939, 1767434768308),
                QueueEntry("heavensdoor", ArcaeaRatingClass.BEYOND, 9910133, 1767287763224),
                QueueEntry("heavensdoor", ArcaeaRatingClass.BEYOND, 9959003, 1767255830438),
                QueueEntry("judgement", ArcaeaRatingClass.FUTURE, 9952373, 1766465853857),
                QueueEntry("judgement", ArcaeaRatingClass.FUTURE, 9983873, 1767342892864),
                QueueEntry("judgement", ArcaeaRatingClass.FUTURE, 9990840, 1768022442474),
                QueueEntry("kyorenromance", ArcaeaRatingClass.FUTURE, 9912544, 1767342491686),
                QueueEntry("kyorenromance", ArcaeaRatingClass.FUTURE, 9938869, 1767342069703),
                QueueEntry("lilly", ArcaeaRatingClass.ETERNAL, 9901422, 1766399869005),
                QueueEntry("lilly", ArcaeaRatingClass.ETERNAL, 9942084, 1767252939533),
                QueueEntry("mvurbd", ArcaeaRatingClass.FUTURE, 9964627, 1766466376707),
                QueueEntry("rekkaresonance", ArcaeaRatingClass.FUTURE, 9914456, 1768547479513),
                QueueEntry("rekkaresonance", ArcaeaRatingClass.FUTURE, 9935083, 1768752552782),
                QueueEntry("undyingmacula", ArcaeaRatingClass.ETERNAL, 9857587, 1766400157386),
                QueueEntry("viciousheroism", ArcaeaRatingClass.BEYOND, 9835093, 1768114025598),
                QueueEntry("vulcanus", ArcaeaRatingClass.FUTURE, 9907382, 1766662605430),
                QueueEntry("vulcanus", ArcaeaRatingClass.FUTURE, 9913883, 1768549775561),
                QueueEntry("vulcanus", ArcaeaRatingClass.FUTURE, 9943066, 1767342534864),
                QueueEntry("welcomequeen", ArcaeaRatingClass.ETERNAL, 9876369, 1767688685531),
                QueueEntry("welcomequeen", ArcaeaRatingClass.ETERNAL, 9921491, 1767435842280),
                QueueEntry("worldvanquisher", ArcaeaRatingClass.FUTURE, 9956584, 1767343449714),
            )
    }
}
