package xyz.sevive.arcaeaoffline.ocr.rois.extractor

import org.opencv.core.Mat
import org.opencv.core.Rect
import xyz.sevive.arcaeaoffline.ocr.rois.definition.DeviceRois
import kotlin.math.roundToInt

class DeviceRoisExtractor(val rois: DeviceRois, val img: Mat) {
    val pureRect: Rect
        get() = Rect(
            rois.pure[0].roundToInt(),
            rois.pure[1].roundToInt(),
            rois.pure[2].roundToInt(),
            rois.pure[3].roundToInt()
        )

    val pure: Mat
        get() = img.submat(this.pureRect)

    val farRect: Rect
        get() = Rect(
            rois.far[0].roundToInt(),
            rois.far[1].roundToInt(),
            rois.far[2].roundToInt(),
            rois.far[3].roundToInt()
        )

    val far: Mat
        get() = img.submat(this.farRect)

    val lostRect: Rect
        get() = Rect(
            rois.lost[0].roundToInt(),
            rois.lost[1].roundToInt(),
            rois.lost[2].roundToInt(),
            rois.lost[3].roundToInt()
        )
    val lost: Mat
        get() = img.submat(this.lostRect)
}