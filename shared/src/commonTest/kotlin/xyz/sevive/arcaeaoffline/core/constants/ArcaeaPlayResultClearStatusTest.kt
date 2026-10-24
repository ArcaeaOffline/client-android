package xyz.sevive.arcaeaoffline.core.constants

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ArcaeaPlayResultClearStatusTest {
    @Test
    fun clearTypesMapToStatuses() {
        assertEquals(ArcaeaPlayResultClearStatus.TRACK_LOST, ArcaeaPlayResultClearStatus.of(ArcaeaPlayResultClearType.TRACK_LOST))
        assertEquals(ArcaeaPlayResultClearStatus.CLEARED, ArcaeaPlayResultClearStatus.of(ArcaeaPlayResultClearType.NORMAL_CLEAR))
        assertEquals(ArcaeaPlayResultClearStatus.CLEARED, ArcaeaPlayResultClearStatus.of(ArcaeaPlayResultClearType.EASY_CLEAR))
        assertEquals(ArcaeaPlayResultClearStatus.CLEARED, ArcaeaPlayResultClearStatus.of(ArcaeaPlayResultClearType.HARD_CLEAR))
        assertEquals(ArcaeaPlayResultClearStatus.FULL_RECALL, ArcaeaPlayResultClearStatus.of(ArcaeaPlayResultClearType.FULL_RECALL))
        assertEquals(ArcaeaPlayResultClearStatus.PURE_MEMORY, ArcaeaPlayResultClearStatus.of(ArcaeaPlayResultClearType.PURE_MEMORY))
        assertNull(ArcaeaPlayResultClearStatus.of(null))
    }

    @Test
    fun statusesMapBackToClearTypes() {
        assertEquals(
            ArcaeaPlayResultClearType.NORMAL_CLEAR,
            ArcaeaPlayResultClearStatus.CLEARED.toClearType(ArcaeaPlayResultModifier.NORMAL),
        )
        assertEquals(
            ArcaeaPlayResultClearType.EASY_CLEAR,
            ArcaeaPlayResultClearStatus.CLEARED.toClearType(ArcaeaPlayResultModifier.EASY),
        )
        assertEquals(
            ArcaeaPlayResultClearType.HARD_CLEAR,
            ArcaeaPlayResultClearStatus.CLEARED.toClearType(ArcaeaPlayResultModifier.HARD),
        )
        assertEquals(
            ArcaeaPlayResultClearType.TRACK_LOST,
            ArcaeaPlayResultClearStatus.TRACK_LOST.toClearType(ArcaeaPlayResultModifier.HARD),
        )
        assertEquals(
            ArcaeaPlayResultClearType.FULL_RECALL,
            ArcaeaPlayResultClearStatus.FULL_RECALL.toClearType(ArcaeaPlayResultModifier.NORMAL),
        )
        assertEquals(
            ArcaeaPlayResultClearType.PURE_MEMORY,
            ArcaeaPlayResultClearStatus.PURE_MEMORY.toClearType(ArcaeaPlayResultModifier.NORMAL),
        )
    }
}
