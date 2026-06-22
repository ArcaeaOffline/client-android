package xyz.sevive.arcaeaoffline.core.calculators

import kotlin.math.floor
import kotlin.math.max

fun calculateArcaeaScoreRange(
    notes: Int,
    pure: Int,
    far: Int,
): IntRange {
    val singleNoteScore = 10000000.0 / notes
    val actualScore = floor(singleNoteScore * pure + singleNoteScore * 0.5 * far).toInt()
    return actualScore..actualScore + pure
}

fun calculatePlayRating(
    score: Int,
    constant: Int,
): Double {
    if (constant < 0) return 0.0

    return if (score >= 10_000_000) {
        constant / 10.0 + 2
    } else if (score >= 9_800_000) {
        constant / 10.0 + 1 + (score - 9_800_000) / 200_000.0
    } else {
        max(0.0, constant / 10.0 + (score - 9_500_000) / 300_000.0)
    }
}
