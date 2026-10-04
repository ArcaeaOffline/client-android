package xyz.sevive.arcaeaoffline.core.constants

/**
 * Potential scoring rules, keyed by the date (yyyyMMdd) the rule took effect.
 *
 * The key is the persisted form (see the `scoring_mode` property) and orders
 * modes chronologically: append new modes with a larger key, never renumber.
 */
enum class ArcaeaScoringMode(
    val key: Int,
) {
    B30_R10(20170602),
    B50(20260827),
    ;

    companion object {
        /** Returns null when the key belongs to a newer app version or is corrupted. */
        fun fromKey(key: Int): ArcaeaScoringMode? = entries.find { it.key == key }
    }
}
