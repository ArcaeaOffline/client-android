package xyz.sevive.arcaeaoffline.core.database.repositories

import android.content.Context
import androidx.room.Room
import androidx.room.execSQL
import androidx.room.immediateTransaction
import androidx.room.useWriterConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultClearType
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.database.ArcaeaOfflineDatabase
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult

/**
 * B50 calculation over the play results of a real save.
 */
@RunWith(AndroidJUnit4::class)
class PotentialRepositoryTest {
    private lateinit var db: ArcaeaOfflineDatabase
    private lateinit var potentialRepository: PotentialRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, ArcaeaOfflineDatabase::class.java).setDriver(BundledSQLiteDriver()).build()

        potentialRepository =
            PotentialRepositoryImpl(
                PlayResultBestRepositoryImpl(
                    db.playResultBestDao(),
                    PlayResultCalculatedRepositoryImpl(db.playResultDao(), db.songDao(), db.chartInfoDao()),
                ),
                R30EntryRepositoryImpl(db.r30EntryDao()),
            )

        runBlocking { seed() }
    }

    private suspend fun trimPlaysTo(count: Int) =
        db.useWriterConnection { connection ->
            connection.immediateTransaction {
                execSQL("DELETE FROM play_results WHERE id NOT IN (SELECT id FROM play_results ORDER BY id LIMIT $count)")
            }
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

    @Test
    fun b50MatchesTheSave() =
        runBlocking {
            assertEquals(
                629.7836316666667,
                potentialRepository
                    .groups()
                    .first()
                    .b50.total,
                TOLERANCE,
            )
        }

    @Test
    fun b10MatchesTheSave() =
        runBlocking {
            assertEquals(
                127.64542999999999,
                potentialRepository
                    .groups()
                    .first()
                    .b10.total,
                TOLERANCE,
            )
        }

    @Test
    fun b30MatchesTheSave() =
        runBlocking {
            assertEquals(
                373.8710750000001,
                potentialRepository
                    .groups()
                    .first()
                    .b30.total,
                TOLERANCE,
            )
        }

    @Test
    fun b50PrefersTheClearedPlayOverTheHigherScoredTrackLost() =
        runBlocking {
            db.playResultDao().upsertBatch(
                trackLostPlay(),
                clearedPlay(),
            )

            val groups = potentialRepository.groups().first()

            val b50Entry = groups.b50.items.single { it.playResult.songId == "grievouslady" }
            assertEquals(9_970_000, b50Entry.playResult.score)
            val b30Entry = groups.b30.items.single { it.playResult.songId == "grievouslady" }
            assertEquals(10_000_000, b30Entry.playResult.score)
        }

    private fun trackLostPlay() =
        PlayResult(
            id = 0,
            songId = "grievouslady",
            ratingClass = ArcaeaRatingClass.FUTURE,
            score = 10_000_000,
            pure = 10_000,
            far = 0,
            lost = 0,
            date = null,
            maxRecall = 10_000,
            modifier = null,
            clearType = ArcaeaPlayResultClearType.TRACK_LOST,
            comment = null,
        )

    private fun clearedPlay() =
        PlayResult(
            id = 0,
            songId = "grievouslady",
            ratingClass = ArcaeaRatingClass.FUTURE,
            score = 9_970_000,
            pure = 9_940,
            far = 30,
            lost = 0,
            date = null,
            maxRecall = 9_940,
            modifier = null,
            clearType = ArcaeaPlayResultClearType.PURE_MEMORY,
            comment = null,
        )

    @Test
    fun completenessTracksHowManyChartsHavePlayResults() =
        runBlocking {
            val full = potentialRepository.groups().first()
            assertEquals(50, full.b50.items.size)
            assertEquals(10, full.b10.items.size)
            assertEquals(30, full.b30.items.size)
            assertEquals(0, full.r10.items.size)
            assertTrue(full.b50.isComplete)
            assertTrue(full.b10.isComplete)
            assertTrue(full.b30.isComplete)
            assertFalse(full.r10.isComplete)

            // Completeness tests
            trimPlaysTo(12)
            val partial = potentialRepository.groups().first()
            assertEquals(10, partial.b10.items.size)
            assertTrue(partial.b10.isComplete)
            assertFalse(partial.b50.isComplete)
            assertFalse(partial.b30.isComplete)

            trimPlaysTo(5)
            val few = potentialRepository.groups().first()
            assertEquals(5, few.b50.items.size)
            assertFalse(few.b10.isComplete)
            assertFalse(few.b50.isComplete)
        }

    private companion object {
        const val SEED_FILE = "xyz/sevive/arcaeaoffline/core/database/repositories/potential_b50_seed.sql"

        /**
         * Repositories sum play ratings in list order, while the expected values were summed with
         * SQL; double addition depends on that order, and 1e-7 stays far below what the app shows.
         */
        const val TOLERANCE = 1e-7
    }
}
