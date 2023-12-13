package xyz.sevive.arcaeaoffline.core.ocr.device.rois.definition

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
