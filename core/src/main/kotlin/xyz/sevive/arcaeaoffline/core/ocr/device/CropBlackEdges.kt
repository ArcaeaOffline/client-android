package xyz.sevive.arcaeaoffline.core.ocr.device

import co.touchlab.kermit.Logger
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import xyz.sevive.arcaeaoffline.core.ocr.device.CropBlackEdges.Companion.cropOrOriginal
import xyz.sevive.arcaeaoffline.core.ocr.opencv.use

class CropBlackEdges {
    companion object {
        private const val LOG_TAG = "CropBlackEdges"
        private val logger = Logger.withTag(LOG_TAG)

        private fun isBlackEdge(
            imgGraySlice: Mat,
            blackPixelThreshold: Int,
            ratio: Double = 0.6,
        ): Boolean =
            Mat().use { pixelsCompared ->
                Core.compare(
                    imgGraySlice,
                    Scalar(blackPixelThreshold.toDouble()),
                    pixelsCompared,
                    Core.CMP_LT,
                )

                Core.countNonZero(pixelsCompared) > (imgGraySlice.width() * imgGraySlice.height()) * ratio
            }

        private fun getCropRect(
            imgGray: Mat,
            blackPixelThreshold: Int = 25,
        ): Rect {
            val width = imgGray.width()
            val height = imgGray.height()

            var left = 0
            var right = width
            var top = 0
            var bottom = height

            // submat views are passed directly instead of pixel-copying clones;
            // isBlackEdge only reads the slice
            for (i in 0..width) {
                val isBlack = imgGray.submat(Rect(i, 0, 1, height)).use { isBlackEdge(it, blackPixelThreshold) }
                if (!isBlack) break
                left += 1
            }

            for (i in width downTo 0) {
                val isBlack = imgGray.submat(Rect(i - 1, 0, 1, height)).use { isBlackEdge(it, blackPixelThreshold) }
                if (!isBlack) break
                right -= 1
            }

            for (i in 0..height) {
                val isBlack = imgGray.submat(Rect(0, i, width, 1)).use { isBlackEdge(it, blackPixelThreshold) }
                if (!isBlack) break
                top += 1
            }

            for (i in height downTo 0) {
                val isBlack = imgGray.submat(Rect(0, i - 1, width, 1)).use { isBlackEdge(it, blackPixelThreshold) }
                if (!isBlack) break
                bottom -= 1
            }

            assert(right > left) { "CropBlackEdges: cropped width <= 0" }
            assert(bottom > top) { "CropBlackEdges: cropped height <= 0" }

            return Rect(left, top, right - left, bottom - top)
        }

        /**
         * This function would fail if the cropped image has a width or height below 0.
         *
         * Use [cropOrOriginal] if you don't want to handle errors.
         */
        fun crop(
            img: Mat,
            convertFlag: Int = Imgproc.COLOR_BGR2GRAY,
            blackPixelThreshold: Int = 25,
        ): Mat =
            Mat().use { imgGray ->
                Imgproc.cvtColor(img, imgGray, convertFlag)
                val rect = getCropRect(imgGray, blackPixelThreshold)
                img.submat(rect).clone()
            }

        /**
         * This function would try returning the cropped image.
         *
         * If any error occurred during the process, it will return the original image.
         */
        fun cropOrOriginal(
            img: Mat,
            convertFlag: Int = Imgproc.COLOR_BGR2GRAY,
            blackPixelThreshold: Int = 25,
        ): Mat =
            try {
                crop(img, convertFlag, blackPixelThreshold)
            } catch (e: Exception) {
                logger.e(e) { "Error cropping an image" }
                img
            }
    }
}
