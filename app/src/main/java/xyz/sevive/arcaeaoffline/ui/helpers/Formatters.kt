package xyz.sevive.arcaeaoffline.ui.helpers

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.em
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaScoreRatingClass
import xyz.sevive.arcaeaoffline.core.database.entities.Chart
import xyz.sevive.arcaeaoffline.core.database.entities.Difficulty
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
         * Format the given score to a level text.
         *
         * For example, 9900000 > "EX+"
         */
        fun scoreToLevelText(score: Int): String {
            return when {
                score >= 9900000 -> "EX+"
                score >= 9800000 -> "EX"
                score >= 9500000 -> "AA"
                score >= 9200000 -> "A"
                score >= 8900000 -> "B"
                score >= 8600000 -> "C"
                else -> "D"
            }
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

        private fun ratingText(
            ratingClass: Int,
            rating: Int,
            ratingPlus: Boolean,
            constant: Int = 0,
        ): String {
            var text = ArcaeaScoreRatingClass.fromInt(ratingClass).toString()
            text += ' '

            if (constant != 0) {
                val decimalFormat = DecimalFormat("0.0")
                text += decimalFormat.format(constant / 10.0)
            } else {
                text += rating.toString()
                if (ratingPlus) {
                    text += '+'
                }
            }

            return text
        }

        /**
         * Returns the readable rating text for the given difficulty.
         *
         * For example:
         * * `Difficulty(ratingClass=2, rating=2, ratingPlus=false)` > "FUTURE 2"
         * * `Difficulty(ratingClass=2, rating=10, ratingPlus=true)` > "FUTURE 10+"
         */
        fun ratingText(difficulty: Difficulty): String {
            return ratingText(
                ratingClass = difficulty.ratingClass,
                rating = difficulty.rating,
                ratingPlus = difficulty.ratingPlus
            )
        }

        /**
         * Returns the readable rating text for the given chart.
         *
         * If the `constant` is not null, return it.
         * Otherwise, return the `rating` and `ratingPlus` fields.
         *
         * For example:
         * * `Chart(ratingClass=2, rating=2, ratingPlus=false)` > "FUTURE 2"
         * * `Chart(ratingClass=2, rating=10, ratingPlus=true)` > "FUTURE 10+"
         * * `Chart(ratingClass=2, rating=10, ratingPlus=true, constant=108)` > "FUTURE 10.8"
         * * `Chart(ratingClass=2, rating=10, ratingPlus=true, constant=0)` > "FUTURE 10+"
         */
        fun ratingText(chart: Chart): String {
            return ratingText(
                ratingClass = chart.ratingClass,
                rating = chart.rating,
                ratingPlus = chart.ratingPlus,
                constant = chart.constant,
            )
        }
    }
}
