package xyz.sevive.arcaeaoffline.core

import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultClearType
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultModifier
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ClearStatusToClearTypeTest {
    @Test
    fun mapsStatusIntegers() {
        assertEquals(
            ArcaeaPlayResultClearType.TRACK_LOST,
            clearStatusToClearType(0, ArcaeaPlayResultModifier.NORMAL),
        )
        assertEquals(
            ArcaeaPlayResultClearType.NORMAL_CLEAR,
            clearStatusToClearType(1, ArcaeaPlayResultModifier.NORMAL),
        )
        assertEquals(
            ArcaeaPlayResultClearType.EASY_CLEAR,
            clearStatusToClearType(1, ArcaeaPlayResultModifier.EASY),
        )
        assertEquals(
            ArcaeaPlayResultClearType.HARD_CLEAR,
            clearStatusToClearType(1, ArcaeaPlayResultModifier.HARD),
        )
        assertEquals(
            ArcaeaPlayResultClearType.FULL_RECALL,
            clearStatusToClearType(2, ArcaeaPlayResultModifier.NORMAL),
        )
        assertEquals(
            ArcaeaPlayResultClearType.PURE_MEMORY,
            clearStatusToClearType(3, ArcaeaPlayResultModifier.NORMAL),
        )
    }

    @Test
    fun rejectsUnknownStatus() {
        assertFailsWith<IllegalArgumentException> {
            clearStatusToClearType(4, ArcaeaPlayResultModifier.NORMAL)
        }
    }
}
