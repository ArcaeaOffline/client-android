package xyz.sevive.arcaeaoffline.ui.helpers

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.em
import java.math.RoundingMode
import java.text.DecimalFormat

class ArcaeaFormatters {
    companion object {
        private val RATING_PLUS_CONSTANTS = listOf(78, 79, 89, 97, 98, 99, 107, 108, 109)

        /**
         * Format the given potential to text.
         * If the potential is null, return "-.--" instead.
         *
         * @param pattern The pattern to use for the formatting, will be passed to [java.text.DecimalFormat].
         */
        fun potentialToText(
            potential: Double?,
            pattern: String = "0.00",
            roundingMode: RoundingMode = RoundingMode.DOWN
        ): String {
            return if (potential != null) {
                val decimalFormat = DecimalFormat(pattern)
                decimalFormat.roundingMode = roundingMode
                decimalFormat.format(potential)
            } else "-.--"
        }

        /**
         * Format the given constant to a "rating class text".
         * For example, 70 to "7", 109 to "10+".
         *
         * If the constant is null, return "?" instead.
         */
        private fun constantToRatingClassText(constant: Int?): String {
            if (constant == null) return "?"

            var text = (constant / 10).toString()

            if (RATING_PLUS_CONSTANTS.contains(constant)) {
                text += "+"
            }

            return text
        }

        /**
         * Wrapper of [constantToRatingClassText] that returns [AnnotatedString] instead.
         *
         * If the formatted rating class text does not contain "+", return the original text.
         * Otherwise, return an annotated string like `10<small>+</small>`.
         *
         * @see constantToRatingClassText
         */
        fun constantToRatingClassAnnotatedString(constant: Int?): AnnotatedString {
            val text = constantToRatingClassText(constant)

            if (!text.contains("+")) return AnnotatedString(text)

            return buildAnnotatedString {
                append(text.substringBefore("+"))

                withStyle(SpanStyle(fontSize = 0.7.em)) {
                    append('+')
                }
            }
        }
    }
}
