package xyz.sevive.arcaeaoffline.core.ocr.device.rois.definition

/** ROI windows on a screenshot. Every value is `[x, y, w, h]`. */
interface DeviceRois {
    val pure: DoubleArray
    val far: DoubleArray
    val lost: DoubleArray
    val score: DoubleArray
    val ratingClass: DoubleArray
    val maxRecall: DoubleArray
    val jacket: DoubleArray
    val clearStatus: DoubleArray
    val partnerIcon: DoubleArray
    val highScore: DoubleArray
    val highScoreDelta: DoubleArray
    val pureShiny: DoubleArray
    val earlyLate: DoubleArray
    val pureLabel: DoubleArray
    val farLabel: DoubleArray
    val lostLabel: DoubleArray
}
