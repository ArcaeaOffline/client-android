package xyz.sevive.arcaeaoffline.ui.helpers

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.em
import com.ionspin.kotlin.bignum.decimal.DecimalMode
import com.ionspin.kotlin.bignum.decimal.RoundingMode
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.database.entities.Chart
import xyz.sevive.arcaeaoffline.core.database.entities.Difficulty

object ArcaeaFormatters {
    /**
     * Format a score.
     *
     * For example 9_876_543 > "09'876'543"
     */
    fun score(score: Int): String {
        if (score >= 99_999_999) return score.toString()

        val padded = score.toString().padStart(8, '0')
        return "${padded.substring(0, 2)}'${padded.substring(2, 5)}'${padded.substring(5, 8)}"
    }

    /**
     * Format the given potential to text.
     * If the potential is null, return "-.--" instead.
     */
    fun potentialToText(
        potential: Double?,
        decimalMode: DecimalMode = DecimalMode(roundingMode = RoundingMode.TOWARDS_ZERO, scale = 2),
    ): String =
        potential
            ?.toBigDecimal()
            ?.roundToDigitPositionAfterDecimalPoint(decimalMode.scale, decimalMode.roundingMode)
            ?.scale(decimalMode.scale)
            ?.toPlainString() ?: "-.--"

    /**
     * Format the given playResult to a level text.
     *
     * For example, 9900000 > "EX+"
     */
    fun scoreToLevelText(score: Int): String =
        when {
            score >= 9_900_000 -> "EX+"
            score >= 9_800_000 -> "EX"
            score >= 9_500_000 -> "AA"
            score >= 9_200_000 -> "A"
            score >= 8_900_000 -> "B"
            score >= 8_600_000 -> "C"
            else -> "D"
        }

    /**
     * Format the given constant to a "rating class text".
     * For example, 70 to "7", 109 to "10+".
     *
     * If the constant is null, return "?" instead.
     */
    internal fun constantToRatingClassText(constant: Int?): String {
        if (constant == null) return "?"

        val base = constant / 10
        val remainder = constant % 10

        return buildString {
            append(base)
            if (base >= 7 && remainder >= 7) append('+')
        }
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

    internal fun ratingText(
        ratingClass: ArcaeaRatingClass,
        rating: Int,
        ratingPlus: Boolean,
        constant: Int = 0,
    ) = buildString {
        append(ratingClass.toString())
        append(' ')

        if (constant > 0) {
            append((constant.toBigDecimal() / 10.toBigDecimal()).toPlainString())
        } else {
            append(rating.toString())
            if (ratingPlus) {
                append('+')
            }
        }
    }

    /**
     * Returns the readable rating text for the given difficulty.
     *
     * For example:
     * * `Difficulty(ratingClass=2, rating=2, ratingPlus=false)` > "FUTURE 2"
     * * `Difficulty(ratingClass=2, rating=10, ratingPlus=true)` > "FUTURE 10+"
     */
    fun ratingText(difficulty: Difficulty): String =
        ratingText(
            ratingClass = difficulty.ratingClass,
            rating = difficulty.rating,
            ratingPlus = difficulty.ratingPlus,
        )

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
    fun ratingText(chart: Chart): String =
        ratingText(
            ratingClass = chart.ratingClass,
            rating = chart.rating,
            ratingPlus = chart.ratingPlus,
            constant = chart.constant,
        )
}
