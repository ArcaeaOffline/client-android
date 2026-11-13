package xyz.sevive.arcaeaoffline.core.ocr.layouts

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.math.max
import kotlin.math.min

/**
 * A device layout definition: ROI field windows authored on a design canvas.
 *
 * Values in [fields] are `[x, y, w, h]` from the top-left corner of the canvas sized
 * [design], e.g. `[1280, 720]` for a 16:9 design.
 */
@Serializable
data class DeviceLayout(
    val design: List<Int>,
    val fields: Map<String, List<Double>>,
) {
    /**
     * Maps a field window onto a `w * h` screenshot.
     *
     * Screens with the canvas aspect ratio or wider scale the canvas by height and
     * center it horizontally. Narrower ones scale by width and center it vertically.
     * The mapped x is never negative.
     *
     * @throws NoSuchElementException when [field] is not in [fields].
     */
    fun screenRect(
        field: String,
        w: Int,
        h: Int,
    ): DoubleArray {
        val (xDesign, yDesign, wDesign, hDesign) = fields.getValue(field)
        val (designW, designH) = design
        val factor = min(w.toDouble() / designW, h.toDouble() / designH)
        val x = max(0.0, w / 2.0 + (xDesign - designW / 2.0) * factor)
        val y =
            if (w.toDouble() / h >= designW.toDouble() / designH) {
                yDesign * factor
            } else {
                h / 2.0 + (yDesign - designH / 2.0) * factor
            }
        return doubleArrayOf(x, y, wDesign * factor, hDesign * factor)
    }

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        /**
         * Parses the bundled layout JSON of [name] from `ocr/layouts/device/`.
         *
         * @throws IllegalStateException when no layout of [name] is bundled.
         */
        fun loadBuiltin(name: String): DeviceLayout {
            val text =
                DeviceLayout::class.java
                    .getResourceAsStream("/ocr/layouts/device/$name.json")
                    ?.readBytes()
                    ?.decodeToString()
                    ?: throw IllegalStateException("No bundled layout named $name")
            return json.decodeFromString(text)
        }
    }
}
