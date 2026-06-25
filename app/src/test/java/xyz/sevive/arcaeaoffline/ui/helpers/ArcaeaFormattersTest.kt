package xyz.sevive.arcaeaoffline.ui.helpers

import org.junit.Assert.assertEquals
import org.junit.Test
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass

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
    fun constantToRatingClassTextTest() {
        val ranges =
            mapOf(
                10..19 to "1",
                20..29 to "2",
                30..39 to "3",
                40..49 to "4",
                50..59 to "5",
                60..69 to "6",
                70..76 to "7",
                77..79 to "7+",
                80..86 to "8",
                87..89 to "8+",
                90..96 to "9",
                97..99 to "9+",
                100..106 to "10",
                107..109 to "10+",
                110..116 to "11",
                117..119 to "11+",
                120..126 to "12",
            )

        ranges.forEach { (range, expected) ->
            range.forEach { constant ->
                val actual = ArcaeaFormatters.constantToRatingClassText(constant)
                assertEquals(
                    "constant [$constant] should be converted to [$expected], but was [$actual]",
                    expected,
                    actual,
                )
            }
        }
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
}
