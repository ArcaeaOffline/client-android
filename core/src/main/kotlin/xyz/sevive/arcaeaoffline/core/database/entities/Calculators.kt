package xyz.sevive.arcaeaoffline.core.database.entities

import kotlin.math.max

fun calculatePlayRating(
    score: Int,
    constant: Int,
): Double {
    if (constant < 0) return 0.0

    return if (score >= 100000000) {
        constant / 10.0 + 2
    } else if (score >= 9800000) {
        constant / 10.0 + 1 + (score - 9800000) / 200000.0
    } else {
        max(0.0, constant / 10.0 + (score - 9500000) / 300000.0)
    }
}
