package xyz.sevive.arcaeaoffline.core.constants

val ArcaeaPlayResultModifierRange = 0..2

enum class ArcaeaPlayResultModifier(val value: Int) {
    NORMAL(0), EASY(1), HARD(2);

    fun toDisplayString(): String {
        return this.name
    }

    companion object {
        fun fromInt(modifierInt: Int): ArcaeaPlayResultModifier {
            assert(
                modifierInt in ArcaeaPlayResultModifierRange
            ) { "modifierInt should be within the range of [0, 2]" }

            return entries.find { it.value == modifierInt } ?: throw IllegalArgumentException()
        }
    }
}
