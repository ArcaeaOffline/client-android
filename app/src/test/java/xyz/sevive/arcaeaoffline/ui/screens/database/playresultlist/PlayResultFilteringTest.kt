package xyz.sevive.arcaeaoffline.ui.screens.database.playresultlist

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultClearStatus
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultClearType
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaScoreGrade
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaScoringMode
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.core.search.ArcaeaCatalog

class PlayResultFilteringTest {
    @Test
    fun ratingClassFacet() {
        val item = uiItem(ratingClass = ArcaeaRatingClass.FUTURE)

        assertTrue(matches(item, PlayResultFilter(ratingClasses = setOf(ArcaeaRatingClass.FUTURE))))
        assertFalse(matches(item, PlayResultFilter(ratingClasses = setOf(ArcaeaRatingClass.PAST))))
    }

    @Test
    fun scoreGradeFacet() {
        val item = uiItem(score = 10_000_000)

        assertTrue(matches(item, PlayResultFilter(scoreGrades = setOf(ArcaeaScoreGrade.EX_PLUS))))
        assertFalse(matches(item, PlayResultFilter(scoreGrades = setOf(ArcaeaScoreGrade.EX))))
    }

    @Test
    fun clearStatusFacet() {
        val item = uiItem(clearType = ArcaeaPlayResultClearType.NORMAL_CLEAR)

        assertTrue(matches(item, PlayResultFilter(clearStatuses = setOf(ArcaeaPlayResultClearStatus.CLEARED))))
        assertFalse(matches(item, PlayResultFilter(clearStatuses = setOf(ArcaeaPlayResultClearStatus.PURE_MEMORY))))
        assertFalse(
            matches(
                uiItem(clearType = null),
                PlayResultFilter(clearStatuses = setOf(ArcaeaPlayResultClearStatus.CLEARED)),
            ),
        )
    }

    @Test
    fun onlyWarningsFacet() {
        val withWarning = uiItem(score = 0)
        val withoutWarning = uiItem(score = 10_000_000)
        val filter = PlayResultFilter(onlyWarnings = true)

        assertTrue(matches(withWarning, filter))
        assertFalse(matches(withoutWarning, filter))
    }

    @Test
    fun allowedSongIdsFiltersBySongId() {
        val item = uiItem(songId = "a")

        assertTrue(item.matches(null, "", EMPTY_CATALOG, PlayResultFilter()))
        assertTrue(item.matches(setOf("a"), "", EMPTY_CATALOG, PlayResultFilter()))
        assertFalse(item.matches(setOf("b"), "", EMPTY_CATALOG, PlayResultFilter()))
    }

    @Test
    fun queryFallsBackToSongIdWithoutSongData() {
        val loadedWithoutSongs = ArcaeaCatalog(isLoaded = true)

        assertTrue(uiItem(songId = "abc123").matches(null, "ABC", loadedWithoutSongs, PlayResultFilter()))
        assertFalse(uiItem(songId = "xyz").matches(null, "ABC", loadedWithoutSongs, PlayResultFilter()))
    }

    @Test
    fun blankQueryAndUnloadedCatalogDoNotConstrain() {
        val item = uiItem(songId = "xyz")

        assertTrue(item.matches(null, "", ArcaeaCatalog(isLoaded = true), PlayResultFilter()))
        assertTrue(item.matches(null, "ABC", ArcaeaCatalog(), PlayResultFilter()))
    }

    @Test
    fun allowedSongIdsResolve() {
        val catalog =
            ArcaeaCatalog(
                songIdsByBasePackId = mapOf("pack" to setOf("a", "b")),
                isLoaded = true,
            )

        assertNull(PlayResultFilter().allowedSongIds(catalog, null))
        assertEquals(setOf("a", "b"), PlayResultFilter(packIds = setOf("pack")).allowedSongIds(catalog, null))
        assertEquals(
            setOf("b"),
            PlayResultFilter(packIds = setOf("pack")).allowedSongIds(catalog, setOf("b", "c")),
        )
        assertEquals(setOf("b", "c"), PlayResultFilter().allowedSongIds(catalog, setOf("b", "c")))
        assertEquals(
            emptySet<String>(),
            PlayResultFilter(packIds = setOf("missing")).allowedSongIds(catalog, null),
        )
    }

    @Test
    fun facetCountsIgnoreTheirOwnSelection() {
        val futureExPlusCleared =
            uiItem(
                songId = "a",
                ratingClass = ArcaeaRatingClass.FUTURE,
                score = 10_000_000,
                clearType = ArcaeaPlayResultClearType.NORMAL_CLEAR,
            )
        val pastExPureMemory =
            uiItem(
                songId = "b",
                ratingClass = ArcaeaRatingClass.PAST,
                score = 9_800_000,
                clearType = ArcaeaPlayResultClearType.PURE_MEMORY,
            )
        val items = listOf(futureExPlusCleared, pastExPureMemory)

        val byRating =
            computeFacetCounts(
                items,
                PlayResultFilter(ratingClasses = setOf(ArcaeaRatingClass.FUTURE)),
                null,
                "",
                EMPTY_CATALOG,
            )
        assertEquals(
            mapOf(ArcaeaRatingClass.FUTURE to 1, ArcaeaRatingClass.PAST to 1),
            byRating.ratingClasses,
        )
        assertEquals(mapOf(ArcaeaScoreGrade.EX_PLUS to 1), byRating.scoreGrades)
        assertEquals(mapOf(ArcaeaPlayResultClearStatus.CLEARED to 1), byRating.clearStatuses)

        val byScore =
            computeFacetCounts(
                items,
                PlayResultFilter(scoreGrades = setOf(ArcaeaScoreGrade.EX_PLUS)),
                null,
                "",
                EMPTY_CATALOG,
            )
        assertEquals(
            mapOf(ArcaeaScoreGrade.EX_PLUS to 1, ArcaeaScoreGrade.EX to 1),
            byScore.scoreGrades,
        )
        assertEquals(mapOf(ArcaeaRatingClass.FUTURE to 1), byScore.ratingClasses)
        assertEquals(mapOf(ArcaeaPlayResultClearStatus.CLEARED to 1), byScore.clearStatuses)

        val byClearStatus =
            computeFacetCounts(
                items,
                PlayResultFilter(clearStatuses = setOf(ArcaeaPlayResultClearStatus.CLEARED)),
                null,
                "",
                EMPTY_CATALOG,
            )
        assertEquals(
            mapOf(
                ArcaeaPlayResultClearStatus.CLEARED to 1,
                ArcaeaPlayResultClearStatus.PURE_MEMORY to 1,
            ),
            byClearStatus.clearStatuses,
        )
        assertEquals(mapOf(ArcaeaRatingClass.FUTURE to 1), byClearStatus.ratingClasses)
        assertEquals(mapOf(ArcaeaScoreGrade.EX_PLUS to 1), byClearStatus.scoreGrades)
    }

    @Test
    fun facetCountsSkipRecordsWithoutClearStatus() {
        val cleared =
            uiItem(
                songId = "a",
                clearType = ArcaeaPlayResultClearType.NORMAL_CLEAR,
            )
        val missingClearType = uiItem(songId = "b", clearType = null)

        val counts =
            computeFacetCounts(
                listOf(cleared, missingClearType),
                PlayResultFilter(),
                null,
                "",
                EMPTY_CATALOG,
            )

        assertEquals(mapOf(ArcaeaPlayResultClearStatus.CLEARED to 1), counts.clearStatuses)
    }

    private fun uiItem(
        songId: String = "song",
        ratingClass: ArcaeaRatingClass = ArcaeaRatingClass.FUTURE,
        score: Int = 10_000_000,
        clearType: ArcaeaPlayResultClearType? = ArcaeaPlayResultClearType.NORMAL_CLEAR,
    ): PlayResultListUiItem =
        PlayResultListUiItem(
            playResult =
                PlayResult(
                    songId = songId,
                    ratingClass = ratingClass,
                    score = score,
                    clearType = clearType,
                ),
            scoringMode = ArcaeaScoringMode.B30_R10,
        )

    private fun matches(
        item: PlayResultListUiItem,
        filter: PlayResultFilter,
    ): Boolean = item.matches(null, "", EMPTY_CATALOG, filter)

    private companion object {
        private val EMPTY_CATALOG = ArcaeaCatalog()
    }
}
