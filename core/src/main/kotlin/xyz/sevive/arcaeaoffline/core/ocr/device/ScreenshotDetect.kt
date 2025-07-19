package xyz.sevive.arcaeaoffline.core.ocr.device

import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Rect
import org.opencv.core.Scalar
import xyz.sevive.arcaeaoffline.core.ocr.device.rois.definition.DeviceRoisAutoT2

object ScreenshotDetect {
    private val PURPLE_HSV_LOWER = Scalar(110.0, 45.0, 75.0)
    private val PURPLE_HSV_UPPER = Scalar(140.0, 150.0, 175.0)

    private const val ROI_HEIGHT_OFFSET = -510
    private const val ROI_WIDTH_OFFSET = -250
    private const val ROI_HEIGHT = 165

    fun isArcaeaScreenshot(imgHsv: Mat, ratio: Double = 0.65): Boolean {
        val rois = DeviceRoisAutoT2(imgHsv.width(), imgHsv.height())

        val roiRect = Rect(
            0,
            (rois.layoutAreaHMid + ROI_HEIGHT_OFFSET * rois.factor).toInt(),
            (rois.wMid + ROI_WIDTH_OFFSET * rois.factor).toInt(),
            (ROI_HEIGHT * rois.factor).toInt()
        )

        val roi = imgHsv.submat(roiRect)
        Core.inRange(roi, PURPLE_HSV_LOWER, PURPLE_HSV_UPPER, roi)

        return Core.countNonZero(roi) >= roi.width() * roi.height() * ratio
    }
}
