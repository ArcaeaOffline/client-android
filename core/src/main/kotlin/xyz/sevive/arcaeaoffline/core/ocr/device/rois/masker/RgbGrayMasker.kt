package xyz.sevive.arcaeaoffline.core.ocr.device.rois.masker

import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import xyz.sevive.arcaeaoffline.core.ocr.opencv.use

object RgbGrayMasker {
    /** Returns a newly allocated 8UC1 mask with 255 where the BGR channel spread is at most [threshold]. */
    private fun channelSpreadMask(
        roiBgr: Mat,
        threshold: Double = 5.0,
    ): Mat {
        val b = Mat()
        val g = Mat()
        val r = Mat()
        val bgDiff = Mat()
        val brDiff = Mat()
        val grDiff = Mat()
        val maxDiff = Mat()
        val mask = Mat()
        try {
            Core.extractChannel(roiBgr, b, 0)
            Core.extractChannel(roiBgr, g, 1)
            Core.extractChannel(roiBgr, r, 2)
            // the largest pairwise diff is the spread (max − min), so this caps it at [threshold]
            Core.absdiff(b, g, bgDiff)
            Core.absdiff(b, r, brDiff)
            Core.absdiff(g, r, grDiff)
            Core.max(bgDiff, brDiff, maxDiff)
            Core.max(maxDiff, grDiff, maxDiff)
            Core.compare(maxDiff, Scalar(threshold), mask, Core.CMP_LE)
        } finally {
            listOf(b, g, r, bgDiff, brDiff, grDiff, maxDiff).forEach { it.release() }
        }
        return mask
    }

    /**
     * Masks gray pixels: BGR values within `[lower, upper]` whose channel spread
     * (max − min) is at most 5. Pixels outside the spread are zeroed and the image eroded 2×2
     * before the range check.
     *
     * @param lower Lower range of BGR values, e.g. `Scalar(50.0, 50.0, 50.0)`
     * @param upper Upper range of BGR values, e.g. `Scalar(160.0, 160.0, 160.0)`
     */
    fun gray(
        roiBgr: Mat,
        lower: Scalar,
        upper: Scalar,
    ): Mat {
        val masked = Mat()
        channelSpreadMask(roiBgr).use { mask ->
            Core.bitwise_and(roiBgr, roiBgr, masked, mask)
        }
        Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(2.0, 2.0)).use { kernel ->
            Imgproc.erode(masked, masked, kernel)
        }
        Core.inRange(masked, lower, upper, masked)
        return masked
    }
}
