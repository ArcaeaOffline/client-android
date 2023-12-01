package xyz.sevive.arcaeaoffline.constants.arcaea.score

val ArcaeaScoreModifierRange = 0..2

enum class ArcaeaScoreModifier(val value: Int) {
    NORMAL(0), EASY(1), HARD(2);

    fun toDisplayString(): String {
        return this.name
    }

    companion object {
        fun fromInt(modifierInt: Int): ArcaeaScoreModifier {
            assert(
                modifierInt in ArcaeaScoreModifierRange
            ) { "modifierInt should be within the range of [0, 2]" }

            return entries.find { it.value == modifierInt } ?: throw IllegalArgumentException()
        }
    }
}
