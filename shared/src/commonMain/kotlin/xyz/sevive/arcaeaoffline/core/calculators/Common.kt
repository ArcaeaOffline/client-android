package xyz.sevive.arcaeaoffline.core.calculators

import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultClearType
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max

fun calculateScoreRange(
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

/** Bonus added to single-play potential by the v7.0 rules (B50 scoring). */
const val PLAY_RATING_CLEAR_BONUS = 0.2

/**
 * Clear-type bonus of a single play. Any state other than TRACK_LOST earns the
 * bonus; a missing clear type is treated as TRACK_LOST, i.e. no bonus.
 */
fun calculateClearBonus(clearType: ArcaeaPlayResultClearType?): Double =
    if (clearType == null || clearType == ArcaeaPlayResultClearType.TRACK_LOST) 0.0 else PLAY_RATING_CLEAR_BONUS

/**
 * Single-play potential under the v7.0 rules: the score-based value plus the
 * clear-type bonus, with the whole sum floored at zero.
 */
fun calculatePlayRating(
    score: Int,
    constant: Int,
    clearType: ArcaeaPlayResultClearType?,
): Double {
    if (constant < 0) return 0.0

    val bonus = calculateClearBonus(clearType)
    return if (score >= 10_000_000) {
        constant / 10.0 + 2 + bonus
    } else if (score >= 9_800_000) {
        constant / 10.0 + 1 + (score - 9_800_000) / 200_000.0 + bonus
    } else {
        max(0.0, constant / 10.0 + (score - 9_500_000) / 300_000.0 + bonus)
    }
}

/**
 * Calculate a possible score [IntRange] from specified [targetPlayRating] and [constant].
 *
 * If the [targetPlayRating] is invalid or too high, null result will be returned.
 *
 * The core algorithm is provided by Google Gemini.
 */
fun calculateInvertScoreRange(
    targetPlayRating: Double,
    constant: Int,
    tolerance: Double = 1e-3,
): IntRange? {
    if (constant < 0 || targetPlayRating < 0.0) return null

    val base = constant / 10.0

    // Actual constraint of play rating
    val prMin = max(0.0, targetPlayRating - tolerance)
    val prMax = targetPlayRating + tolerance

    if (base + 2.0 < prMin) return null

    val minScore =
        when {
            prMin >= base + 2.0 -> 10_000_000
            prMin >= base + 1.0 -> ceil(9_800_000.0 + (prMin - base - 1.0) * 200_000.0).toInt()
            prMin > 0.0 -> max(0, ceil(9_500_000.0 + (prMin - base) * 300_000.0).toInt())
            else -> 0
        }

    val maxScore =
        when {
            prMax >= base + 2.0 -> Int.MAX_VALUE
            prMax >= base + 1.0 -> floor(9_800_000.0 + (prMax - base - 1.0) * 200_000.0).toInt()
            else -> max(0, floor(9_500_000.0 + (prMax - base) * 300_000.0).toInt())
        }

    return minScore..maxScore
}
