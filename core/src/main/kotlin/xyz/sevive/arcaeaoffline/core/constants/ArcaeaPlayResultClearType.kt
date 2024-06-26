package xyz.sevive.arcaeaoffline.core.constants

val ArcaeaPlayResultClearTypeRange = 0..5

enum class ArcaeaPlayResultClearType(val value: Int) {
    TRACK_LOST(0), NORMAL_CLEAR(1), FULL_RECALL(2), PURE_MEMORY(3), EASY_CLEAR(4), HARD_CLEAR(5);

    fun toDisplayString(): String {
        return this.name.replace('_', ' ')
    }

    companion object {
        fun fromInt(clearTypeInt: Int): ArcaeaPlayResultClearType {
            assert(
                clearTypeInt in ArcaeaPlayResultClearTypeRange
            ) { "clearTypeInt should be within the range of [0, 5]" }

            return entries.find { it.value == clearTypeInt } ?: throw IllegalArgumentException()
        }
    }
}
