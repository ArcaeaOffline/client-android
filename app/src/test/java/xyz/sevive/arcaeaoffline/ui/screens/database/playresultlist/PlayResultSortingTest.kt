package xyz.sevive.arcaeaoffline.ui.screens.database.playresultlist

import org.junit.Assert.assertEquals
import org.junit.Test
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultClearType
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaScoringMode
import xyz.sevive.arcaeaoffline.core.database.entities.ChartInfo
import xyz.sevive.arcaeaoffline.core.database.entities.DifficultyWithSong
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.ui.helpers.UiDisplayChartCacheHolder
import kotlin.time.Instant

class PlayResultSortingTest {
    @Test
    fun titleSortsAscendingAndDescending() {
        val items = listOf(uiItem("B"), uiItem("A"), uiItem("C"))

        assertEquals(
            listOf("A", "B", "C"),
            sortPlayResultList(items, PlayResultSortOrder.ASC, PlayResultSortKey.TITLE).map { it.title },
        )
        assertEquals(
            listOf("C", "B", "A"),
            sortPlayResultList(items, PlayResultSortOrder.DESC, PlayResultSortKey.TITLE).map { it.title },
        )
    }

    @Test
    fun difficultySortsKnownConstants() {
        val items =
            listOf(
                uiItem("105", constant = 105),
                uiItem("107", constant = 107),
                uiItem("106", constant = 106),
            )

        assertEquals(
            listOf("105", "106", "107"),
            sortPlayResultList(items, PlayResultSortOrder.ASC, PlayResultSortKey.DIFFICULTY).map { it.title },
        )
        assertEquals(
            listOf("107", "106", "105"),
            sortPlayResultList(items, PlayResultSortOrder.DESC, PlayResultSortKey.DIFFICULTY).map { it.title },
        )
    }

    @Test
    fun difficultyPlacesUnknownConstantsAboveKnownOnesAtTheSameLevelTop() {
        val items =
            listOf(
                uiItem("known 10.6", constant = 106),
                uiItem("unknown 10", rating = 10, constant = null),
                uiItem("known 10.7", rating = 10, ratingPlus = true, constant = 107),
                uiItem("known 10.9", rating = 10, ratingPlus = true, constant = 109),
                uiItem("unknown 10+", rating = 10, ratingPlus = true, constant = null),
            )

        assertEquals(
            listOf("known 10.6", "unknown 10", "known 10.7", "known 10.9", "unknown 10+"),
            sortPlayResultList(items, PlayResultSortOrder.ASC, PlayResultSortKey.DIFFICULTY).map { it.title },
        )
        assertEquals(
            listOf("unknown 10+", "known 10.9", "known 10.7", "unknown 10", "known 10.6"),
            sortPlayResultList(items, PlayResultSortOrder.DESC, PlayResultSortKey.DIFFICULTY).map { it.title },
        )
    }

    @Test
    fun difficultyKeepsMissingSongDataLastInBothDirections() {
        val items =
            listOf(
                uiItem("known 10.6", constant = 106),
                uiItem("unknown 10", rating = 10, constant = null),
                uiItem("no data", songId = "no data", rating = null),
            )

        assertEquals(
            listOf("known 10.6", "unknown 10", "no data"),
            sortPlayResultList(items, PlayResultSortOrder.ASC, PlayResultSortKey.DIFFICULTY).map { it.title },
        )
        assertEquals(
            listOf("unknown 10", "known 10.6", "no data"),
            sortPlayResultList(items, PlayResultSortOrder.DESC, PlayResultSortKey.DIFFICULTY).map { it.title },
        )
    }

    @Test
    fun difficultyTieBreaksByTitleWithoutReversingTheTitleOrder() {
        val items = listOf(uiItem("B", constant = 106), uiItem("A", constant = 106))

        assertEquals(
            listOf("A", "B"),
            sortPlayResultList(items, PlayResultSortOrder.ASC, PlayResultSortKey.DIFFICULTY).map { it.title },
        )
        assertEquals(
            listOf("A", "B"),
            sortPlayResultList(items, PlayResultSortOrder.DESC, PlayResultSortKey.DIFFICULTY).map { it.title },
        )
    }

    @Test
    fun scoreSortsAscendingAndDescending() {
        val items =
            listOf(
                uiItem("low", score = 9_800_000),
                uiItem("high", score = 10_000_000),
            )

        assertEquals(
            listOf("low", "high"),
            sortPlayResultList(items, PlayResultSortOrder.ASC, PlayResultSortKey.SCORE).map { it.title },
        )
        assertEquals(
            listOf("high", "low"),
            sortPlayResultList(items, PlayResultSortOrder.DESC, PlayResultSortKey.SCORE).map { it.title },
        )
    }

    @Test
    fun dateSortsNullsFirstAscendingAndLastDescending() {
        val items =
            listOf(
                uiItem("old", date = Instant.fromEpochMilliseconds(1_000)),
                uiItem("none", date = null),
                uiItem("new", date = Instant.fromEpochMilliseconds(2_000)),
            )

        assertEquals(
            listOf("none", "old", "new"),
            sortPlayResultList(items, PlayResultSortOrder.ASC, PlayResultSortKey.DATE).map { it.title },
        )
        assertEquals(
            listOf("new", "old", "none"),
            sortPlayResultList(items, PlayResultSortOrder.DESC, PlayResultSortKey.DATE).map { it.title },
        )
    }

    private fun uiItem(
        title: String,
        songId: String = title,
        rating: Int? = 10,
        ratingPlus: Boolean = false,
        constant: Int? = null,
        score: Int = 10_000_000,
        date: Instant? = null,
    ): PlayResultListUiItem {
        val playResult =
            PlayResult(
                songId = songId,
                ratingClass = ArcaeaRatingClass.FUTURE,
                score = score,
                date = date,
                clearType = ArcaeaPlayResultClearType.NORMAL_CLEAR,
            )
        val display =
            rating?.let {
                UiDisplayChartCacheHolder.Entry(
                    difficultyWithSong =
                        DifficultyWithSong(
                            songId = songId,
                            ratingClass = ArcaeaRatingClass.FUTURE,
                            rating = rating,
                            ratingPlus = ratingPlus,
                            title = title,
                            artist = "artist",
                        ),
                    chartInfo =
                        constant?.let {
                            ChartInfo(
                                songId = songId,
                                ratingClass = ArcaeaRatingClass.FUTURE,
                                constant = constant,
                                notes = null,
                            )
                        },
                )
            }

        return PlayResultListUiItem(
            playResult = playResult,
            display = display,
            scoringMode = ArcaeaScoringMode.B30_R10,
        )
    }
}
