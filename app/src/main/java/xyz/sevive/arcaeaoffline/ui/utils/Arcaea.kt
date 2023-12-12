package xyz.sevive.arcaeaoffline.ui.utils

import java.math.RoundingMode
import java.text.DecimalFormat

fun potentialToText(potential: Double?, pattern: String = "0.00"): String {
    return if (potential != null) {
        val decimalFormat = DecimalFormat(pattern)
        decimalFormat.roundingMode = RoundingMode.DOWN
        decimalFormat.format(potential)
    } else "-.--"
}
