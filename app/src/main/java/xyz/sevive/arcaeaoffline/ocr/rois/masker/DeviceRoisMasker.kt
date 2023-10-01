package xyz.sevive.arcaeaoffline.ocr.rois.masker

import org.opencv.core.Mat

interface DeviceRoisMasker {
    fun pure(roiBgr: Mat): Mat
    fun far(roiBgr: Mat): Mat
    fun lost(roiBgr: Mat): Mat
}