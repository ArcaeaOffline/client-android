package xyz.sevive.arcaeaoffline.ocr.rois.definition

interface DeviceRois {
    val pure: DoubleArray
    val far: DoubleArray
    val lost: DoubleArray
}
