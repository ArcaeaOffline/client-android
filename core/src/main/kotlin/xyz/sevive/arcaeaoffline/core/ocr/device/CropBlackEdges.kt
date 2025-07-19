package xyz.sevive.arcaeaoffline.core.ocr.device

import android.util.Log
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import xyz.sevive.arcaeaoffline.core.ocr.device.CropBlackEdges.Companion.cropOrOriginal

class CropBlackEdges {
    companion object {
        private fun isBlackEdge(
            imgGraySlice: Mat,
            blackPixelThreshold: Int,
            ratio: Double = 0.6
        ): Boolean {
            val pixelsCompared = Mat()
            Core.compare(
                imgGraySlice,
                Scalar(blackPixelThreshold.toDouble()),
                pixelsCompared,
                Core.CMP_LT,
            )

            return Core.countNonZero(pixelsCompared) > (imgGraySlice.width() * imgGraySlice.height()) * ratio
        }

        private fun getCropRect(imgGray: Mat, blackPixelThreshold: Int = 25): Rect {
            val width = imgGray.width()
            val height = imgGray.height()

            var left = 0
            var right = width
            var top = 0
            var bottom = height

            for (i in 0..width) {
                val rect = Rect(i, 0, 1, height)
                val column = imgGray.submat(rect).clone()
                if (!isBlackEdge(column, blackPixelThreshold)) break
                left += 1
            }

            for (i in width downTo 0) {
                val rect = Rect(i - 1, 0, 1, height)
                val column = imgGray.submat(rect).clone()
                if (!isBlackEdge(column, blackPixelThreshold)) break
                right -= 1
            }

            for (i in 0..height) {
                val rect = Rect(0, i, width, 1)
                val row = imgGray.submat(rect).clone()
                if (!isBlackEdge(row, blackPixelThreshold)) break
                top += 1
            }

            for (i in height downTo 0) {
                val rect = Rect(0, i - 1, width, 1)
                val row = imgGray.submat(rect).clone()
                if (!isBlackEdge(row, blackPixelThreshold)) break
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
        ): Mat {
            val imgGray = Mat()
            Imgproc.cvtColor(img, imgGray, convertFlag)
            val rect = getCropRect(imgGray, blackPixelThreshold)
            return img.submat(rect).clone()
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
        ): Mat {
            return try {
                crop(img, convertFlag, blackPixelThreshold)
            } catch (e: Exception) {
                Log.e("CropBlackEdges", "Error cropping an image", e)
                img
            }
        }
    }
}
