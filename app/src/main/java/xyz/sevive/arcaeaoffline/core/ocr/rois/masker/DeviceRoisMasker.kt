package xyz.sevive.arcaeaoffline.core.ocr.rois.masker

import org.opencv.core.Mat

interface DeviceRoisMasker {
    fun pure(roiBgr: Mat): Mat
    fun far(roiBgr: Mat): Mat
    fun lost(roiBgr: Mat): Mat
    fun score(roiBgr: Mat): Mat
    fun ratingClassPst(roiBgr: Mat): Mat
    fun ratingClassPrs(roiBgr: Mat): Mat
    fun ratingClassFtr(roiBgr: Mat): Mat
    fun ratingClassByd(roiBgr: Mat): Mat
    fun maxRecall(roiBgr: Mat): Mat
    fun clearStatusTrackLost(roiBgr: Mat): Mat
    fun clearStatusTrackComplete(roiBgr: Mat): Mat
    fun clearStatusFullRecall(roiBgr: Mat): Mat
    fun clearStatusPureMemory(roiBgr: Mat): Mat
}
