package xyz.sevive.arcaeaoffline.datastore

/**
 * Device-dependent parallelism strategy for the OCR queue. Kept separate from
 * [OcrQueuePreferences] (a serializable data class) so the "single source" for
 * these runtime-derived values is not tied to the preferences schema itself.
 */
object OcrQueueParallelism {
    fun defaultCount(): Int = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)

    fun countRange(): IntRange = 1..(Runtime.getRuntime().availableProcessors() * 2).coerceAtLeast(2)
}
