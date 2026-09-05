package xyz.sevive.arcaeaoffline.core.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.database.ArcaeaOfflineDatabase
import xyz.sevive.arcaeaoffline.core.database.daos.DifficultyWithSongDao
import xyz.sevive.arcaeaoffline.core.database.entities.ChartInfo
import xyz.sevive.arcaeaoffline.core.database.entities.Difficulty
import xyz.sevive.arcaeaoffline.core.database.entities.DifficultyWithSong
import xyz.sevive.arcaeaoffline.core.database.entities.Song

@RunWith(AndroidJUnit4::class)
class DifficultyWithSongDaoTest {
    private lateinit var db: ArcaeaOfflineDatabase
    private lateinit var dao: DifficultyWithSongDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room
            .inMemoryDatabaseBuilder(context, ArcaeaOfflineDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.difficultyWithSongDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun song(
        id: String,
        title: String,
        artist: String,
        idx: Int,
    ) = Song(
        idx = idx,
        id = id,
        title = title,
        artist = artist,
        set = "base",
        side = 1,
    )

    private fun difficulty(
        songId: String,
        ratingClass: ArcaeaRatingClass,
        ratingClassAlias: Int? = null,
        title: String? = null,
        artist: String? = null,
        rating: Int = 90,
    ) = Difficulty(
        songId = songId,
        ratingClass = ratingClass,
        ratingClassAlias = ratingClassAlias,
        rating = rating,
        ratingPlus = false,
        chartDesigner = null,
        jacketDesigner = null,
        audioOverride = false,
        jacketOverride = false,
        jacketNight = null,
        title = title,
        artist = artist,
        bg = null,
        bgInverse = null,
        bpm = null,
        bpmBase = null,
        version = null,
        date = null,
    )

    private suspend fun seed() {
        db.songDao().upsertBatch(
            song("songa", "A (in-game)", "Artist A", idx = 1),
            song("songb", "B (in-game)", "Artist B", idx = 2),
        )
        db.difficultyDao().upsertBatch(
            // Overrides both title and artist.
            difficulty("songa", ArcaeaRatingClass.FUTURE, title = "A (custom)", artist = "Custom Artist"),
            difficulty("songa", ArcaeaRatingClass.BEYOND, ratingClassAlias = 1),
            // No overrides: display must fall back to the song-level metadata.
            difficulty("songb", ArcaeaRatingClass.PAST, rating = 30),
            // No chart info row for this one.
            difficulty("songb", ArcaeaRatingClass.FUTURE, rating = 60),
        )
        db.chartInfoDao().insertBatch(
            ChartInfo("songa", ArcaeaRatingClass.FUTURE, constant = 960, notes = 1200),
            ChartInfo("songa", ArcaeaRatingClass.BEYOND, constant = 1080, notes = null),
            ChartInfo("songb", ArcaeaRatingClass.PAST, constant = 410, notes = 500),
        )
    }

    @Test
    fun findReturnsAliasAndSongMetadataFallback() = runBlocking {
        seed()

        val result = dao.find("songa", ArcaeaRatingClass.BEYOND).first()

        assertEquals(
            DifficultyWithSong(
                songId = "songa",
                ratingClass = ArcaeaRatingClass.BEYOND,
                ratingClassAlias = 1,
                rating = 90,
                ratingPlus = false,
                // Difficulty title/artist are null: falls back to song metadata.
                title = "A (in-game)",
                artist = "Artist A",
            ),
            result,
        )
    }

    @Test
    fun findReturnsDifficultyOverridesOverSongMetadata() = runBlocking {
        seed()

        val result = dao.find("songa", ArcaeaRatingClass.FUTURE).first()

        assertEquals("A (custom)", result?.title)
        assertEquals("Custom Artist", result?.artist)
    }

    @Test
    fun findReturnsNullForMissingPair() = runBlocking {
        seed()

        assertNull(dao.find("songa", ArcaeaRatingClass.ETERNAL).first())
    }

    @Test
    fun findAllBySongIdsReturnsAllRowsForQueriedSongs() = runBlocking {
        seed()

        val result = dao.findAllBySongIds(listOf("songa", "songb")).first()

        assertEquals(4, result.size)
        assertEquals(
            setOf(
                "songa" to ArcaeaRatingClass.FUTURE,
                "songa" to ArcaeaRatingClass.BEYOND,
                "songb" to ArcaeaRatingClass.PAST,
                "songb" to ArcaeaRatingClass.FUTURE,
            ),
            result.map { it.songId to it.ratingClass }.toSet(),
        )
    }

    @Test
    fun findAllWithInfoJoinsChartInfoOrderedByConstant() = runBlocking {
        seed()

        val result = dao.findAllWithInfo().first()

        // The chart-info-less difficulty is excluded by the INNER JOIN.
        assertEquals(
            listOf(
                "songb" to ArcaeaRatingClass.PAST,
                "songa" to ArcaeaRatingClass.FUTURE,
                "songa" to ArcaeaRatingClass.BEYOND,
            ),
            result.map { it.difficultyWithSong.songId to it.difficultyWithSong.ratingClass },
        )
        assertEquals(listOf(410, 960, 1080), result.map { it.constant })
        // Null notes survive the join.
        assertNull(result.last().notes)
    }
}
