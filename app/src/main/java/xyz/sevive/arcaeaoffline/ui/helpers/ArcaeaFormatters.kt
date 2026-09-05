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
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClassDisplay
import xyz.sevive.arcaeaoffline.core.database.entities.Difficulty
import xyz.sevive.arcaeaoffline.core.database.entities.DifficultyWithSong

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

    internal fun ratingText(
        ratingClassDisplay: ArcaeaRatingClassDisplay,
        rating: Int,
        ratingPlus: Boolean,
        constant: Int = 0,
    ) = buildString {
        append(ratingClassDisplay.name)
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

    internal fun ratingText(
        ratingClass: ArcaeaRatingClass,
        rating: Int,
        ratingPlus: Boolean,
        constant: Int = 0,
    ) = ratingText(
        ArcaeaRatingClassDisplay.of(ratingClass),
        rating,
        ratingPlus,
        constant,
    )

    /**
     * Returns the readable rating text for the given difficulty.
     *
     * For example:
     * * `Difficulty(ratingClass=2, rating=2, ratingPlus=false)` > "FUTURE 2"
     * * `Difficulty(ratingClass=2, rating=10, ratingPlus=true)` > "FUTURE 10+"
     * * `Difficulty(ratingClass=3, rating=11, ratingPlus=true, ratingClassAlias=1)` > "INSCRIBED 11+"
     */
    fun ratingText(difficulty: Difficulty): String =
        ratingText(
            ArcaeaRatingClassDisplay.of(difficulty.ratingClass, difficulty.ratingClassAlias),
            difficulty.rating,
            difficulty.ratingPlus,
        )

    /**
     * Returns the readable rating text for the given difficulty.
     *
     * If the `constant` is not null, return it.
     * Otherwise, return the `rating` and `ratingPlus` fields.
     *
     * For example:
     * * `DifficultyWithSong(ratingClass=2, rating=2, ratingPlus=false, constant=0)` > "FUTURE 2"
     * * `DifficultyWithSong(ratingClass=2, rating=10, ratingPlus=true, constant=108)` > "FUTURE 10.8"
     * * `DifficultyWithSong(ratingClass=3, rating=11, ratingPlus=true, ratingClassAlias=1)` > "INSCRIBED 11+"
     */
    fun ratingText(
        difficultyWithSong: DifficultyWithSong,
        constant: Int = 0,
    ): String =
        ratingText(
            ArcaeaRatingClassDisplay.of(difficultyWithSong.ratingClass, difficultyWithSong.ratingClassAlias),
            difficultyWithSong.rating,
            difficultyWithSong.ratingPlus,
            constant,
        )
}
