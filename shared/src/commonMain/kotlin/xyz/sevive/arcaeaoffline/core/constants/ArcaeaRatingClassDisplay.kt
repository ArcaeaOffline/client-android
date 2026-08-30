package xyz.sevive.arcaeaoffline.core.constants

enum class ArcaeaRatingClassDisplay(
    val ratingClass: ArcaeaRatingClass,
    val alias: Int? = null,
) {
    PAST(ArcaeaRatingClass.PAST),
    PRESENT(ArcaeaRatingClass.PRESENT),
    FUTURE(ArcaeaRatingClass.FUTURE),
    BEYOND(ArcaeaRatingClass.BEYOND),
    ETERNAL(ArcaeaRatingClass.ETERNAL),
    INSCRIBED(ArcaeaRatingClass.BEYOND, alias = 1),
    ;

    companion object {
        fun of(
            ratingClass: ArcaeaRatingClass,
            alias: Int? = null,
        ): ArcaeaRatingClassDisplay =
            entries.find { it.ratingClass == ratingClass && it.alias == alias }
                ?: entries.first { it.ratingClass == ratingClass && it.alias == null }
    }
}
