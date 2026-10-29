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
        assertEquals("0.000", ArcaeaFormatters.potentialToText(0.0))
        assertEquals("12.000", ArcaeaFormatters.potentialToText(12.0))
        assertEquals("12.050", ArcaeaFormatters.potentialToText(12.05))
        assertEquals("12.054", ArcaeaFormatters.potentialToText(12.054))
        assertEquals("12.055", ArcaeaFormatters.potentialToText(12.055))
        assertEquals("12.055", ArcaeaFormatters.potentialToText(12.0559))
        assertEquals("-.---", ArcaeaFormatters.potentialToText(null))

        // Truncation towards zero at the requested scale
        assertEquals("12.05", ArcaeaFormatters.potentialToText(12.0559, scale = 2))
        assertEquals("-.--", ArcaeaFormatters.potentialToText(null, scale = 2))
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

    @Suppress("KotlinMisorderedAssertEqualsArguments")
    @Test
    fun ratingTextPartsRatingTest() {
        assertEquals(
            RatingTextParts("PAST", "2"),
            ArcaeaFormatters.ratingTextParts(ArcaeaRatingClass.PAST, 2, false),
        )

        assertEquals(
            RatingTextParts("PRESENT", "7+"),
            ArcaeaFormatters.ratingTextParts(ArcaeaRatingClass.PRESENT, 7, true),
        )

        assertEquals(
            RatingTextParts("FUTURE", "10"),
            ArcaeaFormatters.ratingTextParts(ArcaeaRatingClass.FUTURE, 10, false),
        )

        assertEquals(
            RatingTextParts("BEYOND", "11+"),
            ArcaeaFormatters.ratingTextParts(ArcaeaRatingClass.BEYOND, 11, true),
        )
    }

    @Suppress("KotlinMisorderedAssertEqualsArguments")
    @Test
    fun ratingTextPartsConstantTest() {
        assertEquals(
            RatingTextParts("PAST", "2.1"),
            ArcaeaFormatters.ratingTextParts(ArcaeaRatingClass.PAST, 2, false, 21),
        )

        assertEquals(
            RatingTextParts("PRESENT", "7.9"),
            ArcaeaFormatters.ratingTextParts(ArcaeaRatingClass.PRESENT, 7, true, 79),
        )

        assertEquals(
            RatingTextParts("FUTURE", "10.6"),
            ArcaeaFormatters.ratingTextParts(ArcaeaRatingClass.FUTURE, 10, false, 106),
        )

        assertEquals(
            RatingTextParts("BEYOND", "11.8"),
            ArcaeaFormatters.ratingTextParts(ArcaeaRatingClass.BEYOND, 11, true, 118),
        )
    }

    @Suppress("KotlinMisorderedAssertEqualsArguments")
    @Test
    fun ratingTextPartsAliasTest() {
        assertEquals(
            RatingTextParts("INSCRIBED", "11"),
            ArcaeaFormatters.ratingTextParts(ArcaeaRatingClassDisplay.INSCRIBED, 11, false),
        )

        assertEquals(
            RatingTextParts("INSCRIBED", "11.5"),
            ArcaeaFormatters.ratingTextParts(ArcaeaRatingClassDisplay.INSCRIBED, 11, false, 115),
        )

        // Unknown (ratingClass, alias) combos must fall back to the default
        // display form of the rating class, not throw.
        assertEquals(
            RatingTextParts("BEYOND", "12"),
            ArcaeaFormatters.ratingTextParts(
                ArcaeaRatingClassDisplay.of(ArcaeaRatingClass.BEYOND, alias = 2),
                12,
                false,
            ),
        )
    }

    @Test
    fun ratingTextPartsDifficultyWithSongTest() {
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
        assertEquals(
            RatingTextParts("FUTURE", "10+"),
            ArcaeaFormatters.ratingTextParts(difficultyWithSong(ArcaeaRatingClass.FUTURE, 10, true)),
        )

        assertEquals(
            RatingTextParts("FUTURE", "10.8"),
            ArcaeaFormatters.ratingTextParts(difficultyWithSong(ArcaeaRatingClass.FUTURE, 10, true), 108),
        )

        assertEquals(
            RatingTextParts("INSCRIBED", "11.5"),
            ArcaeaFormatters.ratingTextParts(difficultyWithSong(ArcaeaRatingClass.BEYOND, 11, false, ratingClassAlias = 1), 115),
        )
    }
}
