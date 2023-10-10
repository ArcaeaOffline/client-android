package xyz.sevive.arcaeaoffline.ocr.rois

import org.opencv.core.Mat
import org.opencv.core.Rect
import kotlin.math.roundToInt

class DeviceRoisExtractor(val rois: DeviceRois, val img: Mat) {
    private fun doubleArrayRoundToRect(array: DoubleArray): Rect {
        return Rect(
            array[0].roundToInt(),
            array[1].roundToInt(),
            array[2].roundToInt(),
            array[3].roundToInt(),
        )
    }

    val pureRect: Rect
        get() = doubleArrayRoundToRect(rois.pure)

    val pure: Mat
        get() = img.submat(this.pureRect)

    val farRect: Rect
        get() = doubleArrayRoundToRect(rois.far)

    val far: Mat
        get() = img.submat(this.farRect)

    val lostRect: Rect
        get() = doubleArrayRoundToRect(rois.lost)
    val lost: Mat
        get() = img.submat(this.lostRect)

    val scoreRect: Rect
        get() = doubleArrayRoundToRect(rois.score)

    val score: Mat
        get() = img.submat(this.scoreRect)

    val ratingClassRect: Rect
        get() = doubleArrayRoundToRect(rois.ratingClass)

    val ratingClass: Mat
        get() = img.submat(this.ratingClassRect)

    val maxRecallRect: Rect
        get() = doubleArrayRoundToRect(rois.maxRecall)

    val maxRecall: Mat
        get() = img.submat(this.maxRecallRect)

    val jacketRect: Rect
        get() = doubleArrayRoundToRect(rois.jacket)

    val jacket: Mat
        get() = img.submat(this.jacketRect)

    val clearStatusRect: Rect
        get() = doubleArrayRoundToRect(rois.clearStatus)

    val clearStatus: Mat
        get() = img.submat(this.clearStatusRect)

    val partnerIconRect: Rect
        get() = doubleArrayRoundToRect(rois.partnerIcon)

    val partnerIcon: Mat
        get() = img.submat(this.partnerIconRect)
}