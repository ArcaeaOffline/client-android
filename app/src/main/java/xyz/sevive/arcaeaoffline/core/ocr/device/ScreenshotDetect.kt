package xyz.sevive.arcaeaoffline.core.ocr.device

import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Rect
import org.opencv.core.Scalar
import xyz.sevive.arcaeaoffline.core.ocr.device.rois.definition.DeviceRoisAutoT2

class ScreenshotDetect {
    companion object {
        private val PURPLE_HSV_LOWER = Scalar(110.0, 45.0, 75.0)
        private val PURPLE_HSV_UPPER = Scalar(140.0, 150.0, 175.0)

        fun isArcaeaScreenshot(imgHsv: Mat, ratio: Double = 0.65): Boolean {
            val rois = DeviceRoisAutoT2(imgHsv.width(), imgHsv.height())

            val roiX = 0
            val roiY = rois.layoutAreaHMid - 510 * rois.factor
            val roiWidth = rois.wMid - 250 * rois.factor
            val roiHeight = 165 * rois.factor
            val roiRect = Rect(roiX, roiY.toInt(), roiWidth.toInt(), roiHeight.toInt())

            val roi = imgHsv.submat(roiRect).clone()
            val roiFiltered = Mat()
            Core.inRange(roi, PURPLE_HSV_LOWER, PURPLE_HSV_UPPER, roiFiltered)

            return Core.countNonZero(roiFiltered) >= roi.width() * roi.height() * ratio
        }
    }
}
