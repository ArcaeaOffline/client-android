package xyz.sevive.arcaeaoffline.constants.arcaea.score

val ArcaeaScoreClearTypeRange = 0..5

enum class ArcaeaScoreClearType(val value: Int) {
    TRACK_LOST(0), NORMAL_CLEAR(1), FULL_RECALL(2), PURE_MEMORY(3), EASY_CLEAR(4), HARD_CLEAR(5);

    companion object {
        fun fromInt(clearTypeInt: Int): ArcaeaScoreClearType {
            assert(
                clearTypeInt in ArcaeaScoreClearTypeRange
            ) { "clearTypeInt should be within the range of [0, 5]" }

            return values().find { it.value == clearTypeInt } ?: throw IllegalArgumentException()
        }
    }
}
