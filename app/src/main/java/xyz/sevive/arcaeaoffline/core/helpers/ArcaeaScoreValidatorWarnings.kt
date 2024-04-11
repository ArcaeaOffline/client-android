package xyz.sevive.arcaeaoffline.core.helpers

import android.content.Context
import androidx.core.content.res.ResourcesCompat

sealed interface ArcaeaScoreValidatorWarning {
    val id: String

    val title: String
    val titleId: Int

    fun getTitle(context: Context? = null): String {
        if (context == null || this.titleId == ResourcesCompat.ID_NULL) {
            return title
        }
        return context.getString(titleId)
    }

    val message: String?
    val messageId: Int

    fun getMessage(context: Context? = null): String? {
        if (context == null || this.messageId == ResourcesCompat.ID_NULL) {
            return message
        }
        return context.getString(messageId)
    }
}

/**
 * Prerequisites:
 * * Chart info
 *
 * Conditions:
 * * Input score does not match the theoretical range (calculated from PURE FAR counts)
 */
class ArcaeaScoreValidateScoreOutOfRangeWarning : ArcaeaScoreValidatorWarning {
    override val id = "SCORE_OUT_OF_RANGE"

    override val title = "Score out of range"
    override val titleId = ResourcesCompat.ID_NULL

    override val message = null
    override val messageId = ResourcesCompat.ID_NULL
}

/**
 * Prerequisites:
 * * Chart info
 *
 * Conditions:
 * * pure + far + lost > chart.notes
 */
class ArcaeaScoreValidatorPflOverflowWarning : ArcaeaScoreValidatorWarning {
    override val id = "PFL_OVERFLOW"

    override val title = "Notes overflow"
    override val titleId = ResourcesCompat.ID_NULL

    override val message = "pure + far + lost > chart note count"
    override val messageId = ResourcesCompat.ID_NULL
}

/**
 * Prerequisites:
 * * Chart info
 *
 * Conditions:
 * * max recall > chart.notes
 */
class ArcaeaScoreValidatorMaxRecallOverflowWarning : ArcaeaScoreValidatorWarning {
    override val id = "MAX_RECALL_OVERFLOW"

    override val title = "Max recall overflow"
    override val titleId = ResourcesCompat.ID_NULL

    override val message = "max recall > chart note count"
    override val messageId = ResourcesCompat.ID_NULL
}

/**
 * Prerequisites:
 * * Chart info
 * * Clear type & modifier combined != `HARD LOST`
 *
 * Conditions:
 * * pure + far + lost != chart.notes
 */
class ArcaeaScoreValidatorClearPflMismatchWarning : ArcaeaScoreValidatorWarning {
    override val id = "CLEAR_PFL_MISMATCH"

    override val title = "Notes mismatch"
    override val titleId = ResourcesCompat.ID_NULL

    override val message = "pure + far + lost != chart note count"
    override val messageId = ResourcesCompat.ID_NULL
}

/**
 * Prerequisites:
 * * Clear type == `PURE MEMORY`
 *
 * Conditions:
 * * far != 0 || lost != 0
 */
class ArcaeaScoreValidatorPureMemoryFarLostNotZeroWarning : ArcaeaScoreValidatorWarning {
    override val id = "PURE_MEMORY_FAR_LOST_NOT_ZERO"

    override val title = "PURE MEMORY?"
    override val titleId = ResourcesCompat.ID_NULL

    override val message = "A PM play result should have 0 far and 0 lost."
    override val messageId = ResourcesCompat.ID_NULL
}
