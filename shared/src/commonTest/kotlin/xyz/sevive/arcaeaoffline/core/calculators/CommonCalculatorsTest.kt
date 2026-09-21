package xyz.sevive.arcaeaoffline.core.calculators

import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultClearType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class CommonCalculatorsTest {
    @Test
    fun testScoreRange() {
        assertEquals(
            calculateScoreRange(2221, 2221, 0),
            10_000_000..10_002_221,
        )
    }

    @Test
    fun testPlayRating() {
        assertEquals(
            calculatePlayRating(10_002_221, 120),
            14.0,
            0.0,
        )

        assertEquals(
            calculatePlayRating(9_949_633, 111),
            12.848165,
            1e-4,
        )

        assertEquals(
            calculatePlayRating(9_849_089, 111),
            12.345445,
            1e-4,
        )

        assertEquals(
            calculatePlayRating(5_500_000, 120),
            0.0,
            0.0,
        )
    }

    @Test
    fun testInvertPlayRating() {
        val result1 = calculateInvertScoreRange(targetPlayRating = 12.465, constant = 107)
        assertNotNull(result1)
        assert(9_952_935 in result1) { "9_952_935 [10.7] > 12.4647" }

        val result2 = calculateInvertScoreRange(targetPlayRating = 12.345, constant = 111)
        assertNotNull(result2)
        assert(9_849_089 in result2) { "9_849_089 [11.1] > 12.3454" }

        val result3 = calculateInvertScoreRange(targetPlayRating = 14.0, constant = 120)
        assertNotNull(result3)
        assert(10_002_221 in result3) { "10_002_221 [12.0] > 14.0" }

        assertNull(calculateInvertScoreRange(targetPlayRating = 14.0, constant = 80))
    }

    @Test
    fun testPlayRatingClearBonus() {
        val score = 10_000_000
        val constant = 100

        // A missing clear type counts as TRACK_LOST: no bonus
        assertEquals(calculatePlayRating(score, constant, null), 12.0, 0.0)
        assertEquals(calculatePlayRating(score, constant, ArcaeaPlayResultClearType.TRACK_LOST), 12.0, 0.0)

        for (
        clearType in
        listOf(
            ArcaeaPlayResultClearType.NORMAL_CLEAR,
            ArcaeaPlayResultClearType.FULL_RECALL,
            ArcaeaPlayResultClearType.PURE_MEMORY,
            ArcaeaPlayResultClearType.EASY_CLEAR,
            ArcaeaPlayResultClearType.HARD_CLEAR,
        )
        ) {
            assertEquals(calculatePlayRating(score, constant, clearType), 12.2, 0.0)
        }

        // The bonus participates in the floor at zero: a negative base plus
        // bonus stays above zero, while TRACK_LOST floors at zero
        assertEquals(calculatePlayRating(8_000_000, 50, null), 0.0, 0.0)
        assertEquals(calculatePlayRating(8_000_000, 50, ArcaeaPlayResultClearType.NORMAL_CLEAR), 0.2, 0.0)
    }
}
