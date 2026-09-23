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
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import xyz.sevive.arcaeaoffline.core.database.ArcaeaOfflineDatabase

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

    @Test
    fun b50MatchesTheSave() =
        runBlocking {
            // 12.595 truncated to three decimals, the value the app displays
            assertEquals(12.595672633333333, potentialRepository.b50().first(), TOLERANCE)
        }

    @Test
    fun b10MatchesTheSave() =
        runBlocking {
            // 12.764
            assertEquals(12.764543, potentialRepository.b10().first(), TOLERANCE)
        }

    @Test
    fun potentialMatchesTheSave() =
        runBlocking {
            // 12.623
            assertEquals(12.623817694444446, potentialRepository.potential().first(), TOLERANCE)
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
