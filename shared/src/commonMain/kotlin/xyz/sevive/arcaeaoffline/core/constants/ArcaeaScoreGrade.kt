package xyz.sevive.arcaeaoffline.core.constants

/**
 * Score grade thresholds, matching the grade shown on a play result.
 */
enum class ArcaeaScoreGrade(
    val minScore: Int,
) {
    EX_PLUS(9_900_000),
    EX(9_800_000),
    AA(9_500_000),
    A(9_200_000),
    B(8_900_000),
    C(8_600_000),
    D(0),
    ;

    fun toDisplayString(): String = if (this == EX_PLUS) "EX+" else name

    companion object {
        /** Scores below 0 are invalid data but still grade as [D]. */
        fun of(score: Int): ArcaeaScoreGrade = entries.firstOrNull { score >= it.minScore } ?: D
    }
}
