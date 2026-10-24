package xyz.sevive.arcaeaoffline.core.constants

import kotlin.test.Test
import kotlin.test.assertEquals

class ArcaeaScoreGradeTest {
    @Test
    fun gradeBoundaries() {
        assertEquals(ArcaeaScoreGrade.D, ArcaeaScoreGrade.of(0))
        assertEquals(ArcaeaScoreGrade.D, ArcaeaScoreGrade.of(-1))
        assertEquals(ArcaeaScoreGrade.C, ArcaeaScoreGrade.of(8_600_000))
        assertEquals(ArcaeaScoreGrade.C, ArcaeaScoreGrade.of(8_899_999))
        assertEquals(ArcaeaScoreGrade.B, ArcaeaScoreGrade.of(8_900_000))
        assertEquals(ArcaeaScoreGrade.A, ArcaeaScoreGrade.of(9_200_000))
        assertEquals(ArcaeaScoreGrade.AA, ArcaeaScoreGrade.of(9_500_000))
        assertEquals(ArcaeaScoreGrade.EX, ArcaeaScoreGrade.of(9_800_000))
        assertEquals(ArcaeaScoreGrade.EX_PLUS, ArcaeaScoreGrade.of(9_900_000))
        assertEquals(ArcaeaScoreGrade.EX_PLUS, ArcaeaScoreGrade.of(10_000_000))
    }

    @Test
    fun displayStrings() {
        assertEquals("EX+", ArcaeaScoreGrade.EX_PLUS.toDisplayString())
        assertEquals("EX", ArcaeaScoreGrade.EX.toDisplayString())
        assertEquals("AA", ArcaeaScoreGrade.AA.toDisplayString())
        assertEquals("D", ArcaeaScoreGrade.D.toDisplayString())
    }
}
