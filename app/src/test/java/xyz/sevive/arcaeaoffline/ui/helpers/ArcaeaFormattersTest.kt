package xyz.sevive.arcaeaoffline.ui.helpers

import org.junit.Assert.assertEquals
import org.junit.Test
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClassDisplay
import xyz.sevive.arcaeaoffline.core.database.entities.DifficultyWithSong

class ArcaeaFormattersTest {
    @Test
    fun scoreTest() {
        assertEquals("00'000'000", ArcaeaFormatters.score(0))
        assertEquals("09'800'000", ArcaeaFormatters.score(9_800_000))
        assertEquals("10'002'221", ArcaeaFormatters.score(10_002_221))
        assertEquals("100002221", ArcaeaFormatters.score(100_002_221))
    }

    @Test
    fun potentialToTextTest() {
        assertEquals("0.00", ArcaeaFormatters.potentialToText(0.0))
        assertEquals("12.00", ArcaeaFormatters.potentialToText(12.0))
        assertEquals("12.05", ArcaeaFormatters.potentialToText(12.05))
        assertEquals("12.05", ArcaeaFormatters.potentialToText(12.054))
        assertEquals("12.05", ArcaeaFormatters.potentialToText(12.055))
        assertEquals("12.05", ArcaeaFormatters.potentialToText(12.056))
        assertEquals("12.05", ArcaeaFormatters.potentialToText(12.057))
        assertEquals("-.--", ArcaeaFormatters.potentialToText(null))
    }

    @Test
    fun scoreToLevelTextTest() {
        assertEquals("EX+", ArcaeaFormatters.scoreToLevelText(10_002_221))
        assertEquals("EX+", ArcaeaFormatters.scoreToLevelText(9_950_000))
        assertEquals("EX+", ArcaeaFormatters.scoreToLevelText(9_900_000))
        assertEquals("EX", ArcaeaFormatters.scoreToLevelText(9_850_000))
        assertEquals("EX", ArcaeaFormatters.scoreToLevelText(9_800_000))
        assertEquals("AA", ArcaeaFormatters.scoreToLevelText(9_700_000))
        assertEquals("AA", ArcaeaFormatters.scoreToLevelText(9_500_000))
        assertEquals("A", ArcaeaFormatters.scoreToLevelText(9_300_000))
        assertEquals("A", ArcaeaFormatters.scoreToLevelText(9_200_000))
        assertEquals("B", ArcaeaFormatters.scoreToLevelText(9_100_000))
        assertEquals("B", ArcaeaFormatters.scoreToLevelText(8_900_000))
        assertEquals("C", ArcaeaFormatters.scoreToLevelText(8_600_000))
        assertEquals("D", ArcaeaFormatters.scoreToLevelText(8_000_000))
        assertEquals("D", ArcaeaFormatters.scoreToLevelText(5_000_000))
        assertEquals("D", ArcaeaFormatters.scoreToLevelText(0))
    }

    @Test
    fun ratingTextRatingTest() {
        assertEquals(
            "PAST 2",
            ArcaeaFormatters.ratingText(ArcaeaRatingClass.PAST, 2, false),
        )

        assertEquals(
            "PRESENT 7+",
            ArcaeaFormatters.ratingText(ArcaeaRatingClass.PRESENT, 7, true),
        )

        assertEquals(
            "FUTURE 10",
            ArcaeaFormatters.ratingText(ArcaeaRatingClass.FUTURE, 10, false),
        )

        assertEquals(
            "BEYOND 11+",
            ArcaeaFormatters.ratingText(ArcaeaRatingClass.BEYOND, 11, true),
        )
    }

    @Test
    fun ratingTextConstantTest() {
        assertEquals(
            "PAST 2.1",
            ArcaeaFormatters.ratingText(ArcaeaRatingClass.PAST, 2, false, 21),
        )

        assertEquals(
            "PRESENT 7.9",
            ArcaeaFormatters.ratingText(ArcaeaRatingClass.PRESENT, 7, true, 79),
        )

        assertEquals(
            "FUTURE 10.6",
            ArcaeaFormatters.ratingText(ArcaeaRatingClass.FUTURE, 10, false, 106),
        )

        assertEquals(
            "BEYOND 11.8",
            ArcaeaFormatters.ratingText(ArcaeaRatingClass.BEYOND, 11, true, 118),
        )
    }

    @Test
    fun ratingTextAliasTest() {
        assertEquals(
            "INSCRIBED 11",
            ArcaeaFormatters.ratingText(ArcaeaRatingClassDisplay.INSCRIBED, 11, false),
        )

        assertEquals(
            "INSCRIBED 11.5",
            ArcaeaFormatters.ratingText(ArcaeaRatingClassDisplay.INSCRIBED, 11, false, 115),
        )

        // Unknown (ratingClass, alias) combos must fall back to the default
        // display form of the rating class, not throw.
        assertEquals(
            "BEYOND 12",
            ArcaeaFormatters.ratingText(
                ArcaeaRatingClassDisplay.of(ArcaeaRatingClass.BEYOND, alias = 2),
                12,
                false,
            ),
        )
    }

    @Test
    fun ratingTextDifficultyWithSongTest() {
        fun difficultyWithSong(
            ratingClass: ArcaeaRatingClass,
            rating: Int,
            ratingPlus: Boolean,
            ratingClassAlias: Int? = null,
        ) = DifficultyWithSong(
            songId = "test",
            ratingClass = ratingClass,
            ratingClassAlias = ratingClassAlias,
            rating = rating,
            ratingPlus = ratingPlus,
            title = "Test",
            artist = "Test",
        )

        // No constant, falling back to rating + ratingPlus.
        assertEquals("FUTURE 10+", ArcaeaFormatters.ratingText(difficultyWithSong(ArcaeaRatingClass.FUTURE, 10, true)))

        assertEquals(
            "FUTURE 10.8",
            ArcaeaFormatters.ratingText(difficultyWithSong(ArcaeaRatingClass.FUTURE, 10, true), 108),
        )

        assertEquals(
            "INSCRIBED 11.5",
            ArcaeaFormatters.ratingText(difficultyWithSong(ArcaeaRatingClass.BEYOND, 11, false, ratingClassAlias = 1), 115),
        )
    }
}
