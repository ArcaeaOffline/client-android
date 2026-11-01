package xyz.sevive.arcaeaoffline.core.dao

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.database.ArcaeaOfflineDatabase
import xyz.sevive.arcaeaoffline.core.database.daos.PlayResultDao
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class PlayResultDaoTest {
    private lateinit var db: ArcaeaOfflineDatabase
    private lateinit var dao: PlayResultDao

    @BeforeTest
    fun setUp() {
        db =
            Room
                .inMemoryDatabaseBuilder<ArcaeaOfflineDatabase>()
                .setDriver(BundledSQLiteDriver())
                .build()
        dao = db.playResultDao()
    }

    @AfterTest
    fun tearDown() {
        db.close()
    }

    private suspend fun seed() {
        db.playResultDao().upsertBatch(
            PlayResult(
                songId = "songa",
                ratingClass = ArcaeaRatingClass.FUTURE,
                score = 9_900_000,
                date = Instant.fromEpochMilliseconds(2000),
            ),
            PlayResult(
                songId = "songa",
                ratingClass = ArcaeaRatingClass.BEYOND,
                score = 9_800_000,
                date = Instant.fromEpochMilliseconds(3000),
            ),
            PlayResult(
                songId = "songb",
                ratingClass = ArcaeaRatingClass.PAST,
                score = 9_700_000,
                date = Instant.fromEpochMilliseconds(1000),
            ),
        )
    }

    @Test
    fun findLatestPlaysOrdersByDateDescendingAndLimits() =
        runBlocking {
            seed()

            val result = dao.findLatestPlays(2).first()

            assertEquals(
                listOf(
                    "songa" to ArcaeaRatingClass.BEYOND,
                    "songa" to ArcaeaRatingClass.FUTURE,
                ),
                result.map { it.songId to it.ratingClass },
            )
        }

    @Test
    fun findLatestPlaysSortsNullDatesLast() =
        runBlocking {
            seed()
            db.playResultDao().upsert(
                PlayResult(songId = "songb", ratingClass = ArcaeaRatingClass.FUTURE, score = 9_600_000, date = null),
            )

            val result = dao.findLatestPlays(10).first()

            assertEquals(listOf(3000L, 2000L, 1000L, null), result.map { it.date?.toEpochMilliseconds() })
            assertNull(result.last().date)
        }
}
