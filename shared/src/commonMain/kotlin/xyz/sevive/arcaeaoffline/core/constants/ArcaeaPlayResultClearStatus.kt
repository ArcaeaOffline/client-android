package xyz.sevive.arcaeaoffline.core.constants

/**
 * The four-state clear result shown by the game.
 */
enum class ArcaeaPlayResultClearStatus {
    TRACK_LOST,
    CLEARED,
    FULL_RECALL,
    PURE_MEMORY,
    ;

    fun toClearType(modifier: ArcaeaPlayResultModifier): ArcaeaPlayResultClearType =
        when (this) {
            TRACK_LOST -> {
                ArcaeaPlayResultClearType.TRACK_LOST
            }

            CLEARED -> {
                when (modifier) {
                    ArcaeaPlayResultModifier.NORMAL -> ArcaeaPlayResultClearType.NORMAL_CLEAR
                    ArcaeaPlayResultModifier.EASY -> ArcaeaPlayResultClearType.EASY_CLEAR
                    ArcaeaPlayResultModifier.HARD -> ArcaeaPlayResultClearType.HARD_CLEAR
                }
            }

            FULL_RECALL -> {
                ArcaeaPlayResultClearType.FULL_RECALL
            }

            PURE_MEMORY -> {
                ArcaeaPlayResultClearType.PURE_MEMORY
            }
        }

    companion object {
        fun of(clearType: ArcaeaPlayResultClearType?): ArcaeaPlayResultClearStatus? =
            when (clearType) {
                null -> null

                ArcaeaPlayResultClearType.TRACK_LOST -> TRACK_LOST

                ArcaeaPlayResultClearType.NORMAL_CLEAR,
                ArcaeaPlayResultClearType.EASY_CLEAR,
                ArcaeaPlayResultClearType.HARD_CLEAR,
                -> CLEARED

                ArcaeaPlayResultClearType.FULL_RECALL -> FULL_RECALL

                ArcaeaPlayResultClearType.PURE_MEMORY -> PURE_MEMORY
            }
    }
}
