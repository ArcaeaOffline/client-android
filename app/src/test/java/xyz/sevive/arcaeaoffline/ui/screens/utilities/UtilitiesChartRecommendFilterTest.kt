package xyz.sevive.arcaeaoffline.ui.screens.utilities

import org.junit.Assert.assertEquals
import org.junit.Test
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultClearType
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.database.entities.DifficultyWithSong
import xyz.sevive.arcaeaoffline.core.database.entities.DifficultyWithSongAndInfo

class UtilitiesChartRecommendFilterTest {
    private fun row(
        songId: String,
        constant: Int,
    ) = DifficultyWithSongAndInfo(
        DifficultyWithSong(songId, ArcaeaRatingClass.FUTURE, null, 5, false, songId, "artist"),
        constant,
        null,
    )

    private val rows = listOf(row("c100", 100), row("c0", 0))

    // Over the EX range, the constant-100 chart earns 11.0 to ~11.5 without the
    // bonus and 11.2 to ~11.7 with it
    private val scoreRange = 9_800_000..9_899_999

    @Test
    fun filterWithoutClearBonus() {
        assertEquals(
            listOf("c100"),
            filterChartsByTarget(rows, scoreRange, 11.05, clearType = null).map { it.difficultyWithSong.songId },
        )

        // Above the bonus-free maximum
        assertEquals(emptyList<DifficultyWithSongAndInfo>(), filterChartsByTarget(rows, scoreRange, 11.65, clearType = null))
    }

    @Test
    fun filterWithClearBonus() {
        // The +0.2 bonus lifts the achievable ratings past the low target
        assertEquals(
            emptyList<DifficultyWithSongAndInfo>(),
            filterChartsByTarget(rows, scoreRange, 11.05, clearType = ArcaeaPlayResultClearType.NORMAL_CLEAR),
        )

        // ...and reaches targets the bonus-free play cannot
        assertEquals(
            listOf("c100"),
            filterChartsByTarget(rows, scoreRange, 11.65, clearType = ArcaeaPlayResultClearType.NORMAL_CLEAR)
                .map { it.difficultyWithSong.songId },
        )
    }

    @Test
    fun filterDropsInvalidConstants() {
        val rowsWithInvalidOnly = listOf(row("c0", 0))

        // A missing chart info reads as constant 0 and earns a rating of 0.0,
        // which no positive target matches
        assertEquals(
            emptyList<DifficultyWithSongAndInfo>(),
            filterChartsByTarget(rowsWithInvalidOnly, scoreRange, 11.0, clearType = null),
        )
    }
}
