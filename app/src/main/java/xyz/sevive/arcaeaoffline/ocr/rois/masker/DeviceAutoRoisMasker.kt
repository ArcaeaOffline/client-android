package xyz.sevive.arcaeaoffline.ocr.rois.masker

import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc

interface DeviceAutoRoisMasker : DeviceRoisMasker {}

class DeviceAutoRoisMaskerT2 : DeviceAutoRoisMasker {
    val PFL_HSV_MIN = Scalar(0.0, 0.0, 248.0)
    val PFL_HSV_MAX = Scalar(179.0, 10.0, 255.0)

    private fun pfl(roiBgr: Mat): Mat {
        val roiHsv = roiBgr.clone()
        val dst = roiBgr.clone()
        Imgproc.cvtColor(roiBgr, roiHsv, Imgproc.COLOR_BGR2HSV)
        Core.inRange(roiHsv, this.PFL_HSV_MIN, this.PFL_HSV_MAX, dst)
        return dst
    }

    override fun pure(roiBgr: Mat): Mat {
        return this.pfl(roiBgr)
    }

    override fun far(roiBgr: Mat): Mat {
        return this.pfl(roiBgr)
    }

    override fun lost(roiBgr: Mat): Mat {
        return this.pfl(roiBgr)
    }
}
