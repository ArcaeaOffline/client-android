package xyz.sevive.arcaeaoffline.core.ocr.device

import co.touchlab.kermit.Logger
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
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

            // submat views are passed directly instead of pixel-copying clones.
            // isBlackEdge only reads the slice
            for (i in 0 until width) {
                val isBlack = imgGray.submat(Rect(i, 0, 1, height)).use { isBlackEdge(it, blackPixelThreshold) }
                if (!isBlack) break
                left += 1
            }

            // keeps at least a two-column crop whenever a non-black column exists
            for (i in width - 1 downTo 0) {
                if (i <= left + 1) break
                val isBlack = imgGray.submat(Rect(i, 0, 1, height)).use { isBlackEdge(it, blackPixelThreshold) }
                if (!isBlack) break
                right -= 1
            }

            for (i in 0 until height) {
                val isBlack = imgGray.submat(Rect(0, i, width, 1)).use { isBlackEdge(it, blackPixelThreshold) }
                if (!isBlack) break
                top += 1
            }

            for (i in height - 1 downTo 0) {
                if (i <= top + 1) break
                val isBlack = imgGray.submat(Rect(0, i, width, 1)).use { isBlackEdge(it, blackPixelThreshold) }
                if (!isBlack) break
                bottom -= 1
            }

            require(right > left) { "CropBlackEdges: cropped width <= 0" }
            require(bottom > top) { "CropBlackEdges: cropped height <= 0" }

            return Rect(left, top, right - left, bottom - top)
        }

        /**
         * Crops the black edges of [img].
         *
         * @throws IllegalArgumentException when the image is fully black.
         *
         * Use [cropOrOriginal] to fall back to the original image instead of throwing.
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
