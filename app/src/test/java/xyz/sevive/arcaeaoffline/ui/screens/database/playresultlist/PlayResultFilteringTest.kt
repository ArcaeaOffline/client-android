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
import xyz.sevive.arcaeaoffline.core.database.entities.Song
import xyz.sevive.arcaeaoffline.core.search.ArcaeaCatalog
import xyz.sevive.arcaeaoffline.core.search.ArcaeaCatalogSong
import xyz.sevive.arcaeaoffline.core.search.searchDocument

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
    fun allowedPackSongIdsFiltersBySongId() {
        val item = uiItem(songId = "a")

        assertTrue(item.matches(null, null, "", EMPTY_CATALOG, PlayResultFilter()))
        assertTrue(item.matches(setOf("a"), null, "", EMPTY_CATALOG, PlayResultFilter()))
        assertFalse(item.matches(setOf("b"), null, "", EMPTY_CATALOG, PlayResultFilter()))
    }

    @Test
    fun queryFallsBackToSongIdWithoutSongData() {
        val loadedWithoutSongs = ArcaeaCatalog(isLoaded = true)

        assertTrue(uiItem(songId = "abc123").matches(null, null, "ABC", loadedWithoutSongs, PlayResultFilter()))
        assertFalse(uiItem(songId = "xyz").matches(null, null, "ABC", loadedWithoutSongs, PlayResultFilter()))
    }

    @Test
    fun blankQueryAndUnloadedCatalogDoNotConstrain() {
        val item = uiItem(songId = "xyz")

        assertTrue(item.matches(null, null, "", ArcaeaCatalog(isLoaded = true), PlayResultFilter()))
        assertTrue(item.matches(null, null, "ABC", ArcaeaCatalog(), PlayResultFilter()))
    }

    @Test
    fun commentTokensMatchInAnyOrder() {
        val item = uiItem(comment = "OCR IMG_20240101_120000.jpg")
        val loaded = ArcaeaCatalog(isLoaded = true)

        assertTrue(item.matches(null, null, "OCR IMG", loaded, PlayResultFilter()))
        assertTrue(item.matches(null, null, "IMG OCR", loaded, PlayResultFilter()))
        assertTrue(item.matches(null, null, "20240101", loaded, PlayResultFilter()))
        assertFalse(item.matches(null, null, "missing", loaded, PlayResultFilter()))
    }

    @Test
    fun commentMatchIgnoresFullWidthAndCase() {
        val item = uiItem(comment = "ＯＣＲ Ｎｏｖａ")
        val loaded = ArcaeaCatalog(isLoaded = true)

        assertTrue(item.matches(null, null, "ocr nova", loaded, PlayResultFilter()))
    }

    @Test
    fun commentMatchIsOrWithSongMatch() {
        val catalog =
            ArcaeaCatalog(
                songs =
                    listOf("a", "b").map { id ->
                        ArcaeaCatalogSong(
                            song = Song(idx = 0, id = id, title = id, artist = "", set = "", side = 0),
                            pack = null,
                            document = searchDocument(id) {},
                        )
                    },
                isLoaded = true,
            )
        val item = uiItem(songId = "b", comment = "OCR IMG.jpg")

        assertTrue(item.matches(null, setOf("a"), "IMG", catalog, PlayResultFilter()))
        assertTrue(item.matches(null, setOf("b"), "missing", catalog, PlayResultFilter()))
        assertFalse(item.matches(null, setOf("a"), "missing", catalog, PlayResultFilter()))
    }

    @Test
    fun missingCommentNeverMatches() {
        val loaded = ArcaeaCatalog(isLoaded = true)

        assertFalse(uiItem(comment = null).matches(null, null, "OCR", loaded, PlayResultFilter()))
    }

    @Test
    fun allowedPackSongIdsResolve() {
        val catalog =
            ArcaeaCatalog(
                songIdsByBasePackId = mapOf("pack" to setOf("a", "b")),
                isLoaded = true,
            )

        assertNull(PlayResultFilter().allowedPackSongIds(catalog))
        assertEquals(setOf("a", "b"), PlayResultFilter(packIds = setOf("pack")).allowedPackSongIds(catalog))
        assertEquals(
            emptySet<String>(),
            PlayResultFilter(packIds = setOf("missing")).allowedPackSongIds(catalog),
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
        comment: String? = null,
    ): PlayResultListUiItem =
        PlayResultListUiItem(
            playResult =
                PlayResult(
                    songId = songId,
                    ratingClass = ratingClass,
                    score = score,
                    clearType = clearType,
                    comment = comment,
                ),
            scoringMode = ArcaeaScoringMode.B30_R10,
        )

    private fun matches(
        item: PlayResultListUiItem,
        filter: PlayResultFilter,
    ): Boolean = item.matches(null, null, "", EMPTY_CATALOG, filter)

    private companion object {
        private val EMPTY_CATALOG = ArcaeaCatalog()
    }
}
