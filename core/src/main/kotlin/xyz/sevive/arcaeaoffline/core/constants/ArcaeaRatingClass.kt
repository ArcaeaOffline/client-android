package xyz.sevive.arcaeaoffline.core.constants

val ArcaeaRatingClassRange = 0..4

enum class ArcaeaRatingClass(val value: Int) {
    PAST(0), PRESENT(1), FUTURE(2), BEYOND(3), ETERNAL(4);

    companion object {
        fun fromInt(ratingClassInt: Int): ArcaeaRatingClass {
            assert(
                ratingClassInt in ArcaeaRatingClassRange
            ) { "ratingClassInt should be within the range of [0, 4]" }

            return entries.find { it.value == ratingClassInt } ?: throw IllegalArgumentException()
        }
    }
}
