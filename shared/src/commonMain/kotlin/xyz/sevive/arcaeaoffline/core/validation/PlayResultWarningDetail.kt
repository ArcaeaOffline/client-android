package xyz.sevive.arcaeaoffline.core.validation

/**
 * The values a warning compared. [None] means the warning is raised but the compared values were
 * incomplete, so there is no evidence to show.
 */
sealed interface PlayResultWarningDetail {
    data object None : PlayResultWarningDetail

    data class ScoreOutOfRange(
        val actual: Int,
        val expected: IntRange,
        val pure: Int,
        val far: Int,
    ) : PlayResultWarningDetail

    data class ScoreTooLow(
        val actual: Int,
        val minimum: Int,
    ) : PlayResultWarningDetail

    data class ScoreNegative(
        val actual: Int,
    ) : PlayResultWarningDetail

    data class ScoreBeyondMaximum(
        val actual: Int,
        val maximum: Int,
    ) : PlayResultWarningDetail

    data class Judgments(
        val pure: Int,
        val far: Int,
        val lost: Int,
        val notes: Int,
    ) : PlayResultWarningDetail

    /**
     * [expected] is the tightest allowed value: the upper bound for
     * [PlayResultWarning.MAX_RECALL_OVERFLOW], the chart's note count for
     * [PlayResultWarning.FR_PM_MAX_RECALL_MISMATCH].
     */
    data class MaxRecall(
        val actual: Int,
        val expected: Int,
    ) : PlayResultWarningDetail

    data class FarLost(
        val far: Int,
        val lost: Int,
    ) : PlayResultWarningDetail

    data class Lost(
        val actual: Int,
    ) : PlayResultWarningDetail
}
