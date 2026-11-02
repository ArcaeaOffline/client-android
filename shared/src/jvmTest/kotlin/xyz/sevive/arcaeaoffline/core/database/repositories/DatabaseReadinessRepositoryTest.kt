package xyz.sevive.arcaeaoffline.core.database.repositories

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.database.ArcaeaOfflineDatabase
import xyz.sevive.arcaeaoffline.core.database.entities.ChartInfo
import xyz.sevive.arcaeaoffline.core.database.entities.Difficulty
import xyz.sevive.arcaeaoffline.core.database.entities.Song
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DatabaseReadinessRepositoryTest {
    private lateinit var db: ArcaeaOfflineDatabase
    private lateinit var repository: DatabaseReadinessRepository

    @BeforeTest
    fun setUp() {
        db =
            Room
                .inMemoryDatabaseBuilder<ArcaeaOfflineDatabase>()
                .setDriver(BundledSQLiteDriver())
                .build()
        repository =
            DatabaseReadinessRepositoryImpl(
                songRepo = SongRepositoryImpl(db.songDao()),
                chartInfoRepo = ChartInfoRepositoryImpl(db.chartInfoDao()),
                difficultyWithSongRepo = DifficultyWithSongRepositoryImpl(db.difficultyWithSongDao()),
            )
    }

    @AfterTest
    fun tearDown() {
        db.close()
    }

    private fun song(
        id: String,
        idx: Int,
    ) = Song(
        idx = idx,
        id = id,
        title = id,
        artist = "Artist",
        set = "base",
        side = 1,
    )

    private fun difficulty(
        songId: String,
        ratingClass: ArcaeaRatingClass,
    ) = Difficulty(
        songId = songId,
        ratingClass = ratingClass,
        ratingClassAlias = null,
        rating = 90,
        ratingPlus = false,
        chartDesigner = null,
        jacketDesigner = null,
        audioOverride = false,
        jacketOverride = false,
        jacketNight = null,
        title = null,
        artist = null,
        bg = null,
        bgInverse = null,
        bpm = null,
        bpmBase = null,
        version = null,
        date = null,
    )

    // songb/FUTURE is left without chart info.
    private suspend fun seedPartial() {
        db.songDao().upsertBatch(song("songa", idx = 1), song("songb", idx = 2))
        db.difficultyDao().upsertBatch(
            difficulty("songa", ArcaeaRatingClass.FUTURE),
            difficulty("songa", ArcaeaRatingClass.BEYOND),
            difficulty("songb", ArcaeaRatingClass.PAST),
            difficulty("songb", ArcaeaRatingClass.FUTURE),
        )
        db.chartInfoDao().insertBatch(
            ChartInfo("songa", ArcaeaRatingClass.FUTURE, constant = 960, notes = 1200),
            ChartInfo("songa", ArcaeaRatingClass.BEYOND, constant = 1080, notes = null),
            ChartInfo("songb", ArcaeaRatingClass.PAST, constant = 410, notes = 500),
        )
    }

    @Test
    fun emptyWhenNoSongs() =
        runBlocking {
            assertEquals(DatabaseReadiness.EMPTY, repository.readiness().first())
            assertEquals(0, repository.missingChartInfoCount().first())
        }

    @Test
    fun songsOnlyWhenNoChartInfo() =
        runBlocking {
            db.songDao().upsertBatch(song("songa", idx = 1))

            assertEquals(DatabaseReadiness.SONGS_ONLY, repository.readiness().first())
        }

    @Test
    fun partialWhenSomeDifficultiesLackChartInfo() =
        runBlocking {
            seedPartial()

            assertEquals(DatabaseReadiness.PARTIAL, repository.readiness().first())
            assertEquals(1, repository.missingChartInfoCount().first())
        }

    @Test
    fun readyWhenEveryDifficultyHasChartInfo() =
        runBlocking {
            seedPartial()
            db.chartInfoDao().insert(
                ChartInfo("songb", ArcaeaRatingClass.FUTURE, constant = 700, notes = 800),
            )

            assertEquals(DatabaseReadiness.READY, repository.readiness().first())
            assertEquals(0, repository.missingChartInfoCount().first())
        }

    @Test
    fun summaryCarriesReadinessAndCounts() =
        runBlocking {
            seedPartial()

            val summary = repository.summary().first()

            assertEquals(
                DatabaseReadinessSummary(
                    readiness = DatabaseReadiness.PARTIAL,
                    songCount = 2,
                    chartInfoCount = 3,
                    missingChartInfoCount = 1,
                ),
                summary,
            )
        }
}
