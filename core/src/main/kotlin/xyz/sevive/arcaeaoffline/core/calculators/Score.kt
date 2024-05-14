package xyz.sevive.arcaeaoffline.core.calculators

import kotlin.math.floor

fun calculateArcaeaScoreRange(notes: Int, pure: Int, far: Int): IntRange {
    val singleNoteScore = 10000000.0 / notes
    val actualScore = floor(singleNoteScore * pure + singleNoteScore * 0.5 * far).toInt()
    return actualScore..actualScore + pure
}
