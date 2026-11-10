package xyz.sevive.arcaeaoffline.ui.helpers

import com.ionspin.kotlin.bignum.decimal.RoundingMode
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClassDisplay
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaScoreGrade
import xyz.sevive.arcaeaoffline.core.database.entities.DifficultyWithSong

/**
 * Rating text split into its rating class display name and level value, for layouts rendering the
 * two separately.
 */
data class RatingTextParts(
    val ratingClass: String,
    val value: String,
)

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
     * Format the given potential to text, truncated (towards zero) to [scale] decimal places.
     * If the potential is null, return a placeholder of the same width.
     */
    fun potentialToText(
        potential: Double?,
        scale: Int = 3,
    ): String =
        potential
            ?.toBigDecimal()
            ?.roundToDigitPositionAfterDecimalPoint(scale.toLong(), RoundingMode.TOWARDS_ZERO)
            ?.scale(scale.toLong())
            ?.toPlainString() ?: ("-." + "-".repeat(scale))

    /**
     * Format the given playResult to a level text.
     *
     * For example, 9900000 > "EX+"
     */
    fun scoreToLevelText(score: Int): String = ArcaeaScoreGrade.of(score).toDisplayString()

    internal fun ratingTextParts(
        ratingClassDisplay: ArcaeaRatingClassDisplay,
        rating: Int,
        ratingPlus: Boolean,
        constant: Int = 0,
    ): RatingTextParts {
        val value =
            if (constant > 0) {
                (constant.toBigDecimal() / 10.toBigDecimal()).toPlainString()
            } else {
                if (ratingPlus) "$rating+" else rating.toString()
            }
        return RatingTextParts(ratingClassDisplay.name, value)
    }

    internal fun ratingTextParts(
        ratingClass: ArcaeaRatingClass,
        rating: Int,
        ratingPlus: Boolean,
        constant: Int = 0,
    ) = ratingTextParts(
        ArcaeaRatingClassDisplay.of(ratingClass),
        rating,
        ratingPlus,
        constant,
    )

    /**
     * Returns the rating class and level value parts for the given difficulty.
     *
     * If the `constant` is positive, return it (divided by 10) as the value.
     * Otherwise, return the `rating` and `ratingPlus` fields.
     *
     * For example:
     * * `DifficultyWithSong(ratingClass=2, rating=2, ratingPlus=false, constant=0)` > `RatingTextParts("FUTURE", "2")`
     * * `DifficultyWithSong(ratingClass=2, rating=10, ratingPlus=true, constant=108)` > `RatingTextParts("FUTURE", "10.8")`
     * * `DifficultyWithSong(ratingClass=3, rating=11, ratingPlus=true, ratingClassAlias=1)` > `RatingTextParts("INSCRIBED", "11+")`
     */
    fun ratingTextParts(
        difficultyWithSong: DifficultyWithSong,
        constant: Int = 0,
    ): RatingTextParts =
        ratingTextParts(
            ArcaeaRatingClassDisplay.of(difficultyWithSong.ratingClass, difficultyWithSong.ratingClassAlias),
            difficultyWithSong.rating,
            difficultyWithSong.ratingPlus,
            constant,
        )
}
