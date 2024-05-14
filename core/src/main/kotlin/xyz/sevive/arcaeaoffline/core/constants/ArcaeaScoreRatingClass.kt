package xyz.sevive.arcaeaoffline.core.constants

val ArcaeaScoreRatingClassRange = 0..4

enum class ArcaeaScoreRatingClass(val value: Int) {
    PAST(0), PRESENT(1), FUTURE(2), BEYOND(3), ETERNAL(4);

    companion object {
        fun fromInt(ratingClassInt: Int): ArcaeaScoreRatingClass {
            assert(
                ratingClassInt in ArcaeaScoreRatingClassRange
            ) { "ratingClassInt should be within the range of [0, 4]" }

            return entries.find { it.value == ratingClassInt } ?: throw IllegalArgumentException()
        }
    }
}