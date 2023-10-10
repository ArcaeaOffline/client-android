package xyz.sevive.arcaeaoffline.ocr.rois

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
}
