package xyz.sevive.arcaeaoffline.ocr.rois

import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc

interface DeviceAutoRoisMasker : DeviceRoisMasker {}

class DeviceAutoRoisMaskerT2 : DeviceAutoRoisMasker {
    val PFL_HSV_MIN = Scalar(0.0, 0.0, 248.0)
    val PFL_HSV_MAX = Scalar(179.0, 10.0, 255.0)

    val WHITE_HSV_MIN = Scalar(0.0, 0.0, 240.0)
    val WHITE_HSV_MAX = Scalar(179.0, 10.0, 255.0)

    val PST_HSV_MIN = Scalar(100.0, 50.0, 80.0)
    val PST_HSV_MAX = Scalar(100.0, 255.0, 255.0)

    val PRS_HSV_MIN = Scalar(43.0, 40.0, 75.0)
    val PRS_HSV_MAX = Scalar(50.0, 155.0, 190.0)

    val FTR_HSV_MIN = Scalar(149.0, 30.0, 0.0)
    val FTR_HSV_MAX = Scalar(155.0, 181.0, 150.0)

    val BYD_HSV_MIN = Scalar(170.0, 50.0, 50.0)
    val BYD_HSV_MAX = Scalar(179.0, 210.0, 198.0)

    val MAX_RECALL_HSV_MIN = Scalar(125.0, 0.0, 0.0)
    val MAX_RECALL_HSV_MAX = Scalar(145.0, 100.0, 150.0)

    val TRACK_LOST_HSV_MIN = Scalar(170.0, 75.0, 90.0)
    val TRACK_LOST_HSV_MAX = Scalar(175.0, 170.0, 160.0)

    val TRACK_COMPLETE_HSV_MIN = Scalar(140.0, 0.0, 50.0)
    val TRACK_COMPLETE_HSV_MAX = Scalar(145.0, 50.0, 130.0)

    val FULL_RECALL_HSV_MIN = Scalar(140.0, 60.0, 80.0)
    val FULL_RECALL_HSV_MAX = Scalar(150.0, 130.0, 145.0)

    val PURE_MEMORY_HSV_MIN = Scalar(90.0, 70.0, 80.0)
    val PURE_MEMORY_HSV_MAX = Scalar(110.0, 200.0, 175.0)

    private fun maskHsv(roiBgr: Mat, hsvMin: Scalar, hsvMax: Scalar): Mat {
        val roiHsv = Mat()
        val dst = Mat()
        Imgproc.cvtColor(roiBgr, roiHsv, Imgproc.COLOR_BGR2HSV)
        Core.inRange(roiHsv, hsvMin, hsvMax, dst)
        return dst
    }

    private fun pfl(roiBgr: Mat): Mat {
        return this.maskHsv(roiBgr, this.PFL_HSV_MIN, this.PFL_HSV_MAX)
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

    override fun score(roiBgr: Mat): Mat {
        return this.maskHsv(roiBgr, this.WHITE_HSV_MIN, this.WHITE_HSV_MAX)
    }

    override fun ratingClassPst(roiBgr: Mat): Mat {
        return this.maskHsv(roiBgr, this.PST_HSV_MIN, this.PST_HSV_MAX)
    }

    override fun ratingClassPrs(roiBgr: Mat): Mat {
        return this.maskHsv(roiBgr, this.PRS_HSV_MIN, this.PRS_HSV_MAX)
    }

    override fun ratingClassFtr(roiBgr: Mat): Mat {
        return this.maskHsv(roiBgr, this.FTR_HSV_MIN, this.FTR_HSV_MAX)
    }

    override fun ratingClassByd(roiBgr: Mat): Mat {
        return this.maskHsv(roiBgr, this.BYD_HSV_MIN, this.BYD_HSV_MAX)
    }

    override fun maxRecall(roiBgr: Mat): Mat {
        return this.maskHsv(roiBgr, this.MAX_RECALL_HSV_MIN, this.MAX_RECALL_HSV_MAX)
    }

    override fun clearStatusTrackLost(roiBgr: Mat): Mat {
        return this.maskHsv(roiBgr, this.TRACK_LOST_HSV_MIN, this.TRACK_LOST_HSV_MAX)
    }

    override fun clearStatusTrackComplete(roiBgr: Mat): Mat {
        return this.maskHsv(roiBgr, this.TRACK_COMPLETE_HSV_MIN, this.TRACK_COMPLETE_HSV_MAX)
    }

    override fun clearStatusFullRecall(roiBgr: Mat): Mat {
        return this.maskHsv(roiBgr, this.FULL_RECALL_HSV_MIN, this.FULL_RECALL_HSV_MAX)
    }

    override fun clearStatusPureMemory(roiBgr: Mat): Mat {
        return this.maskHsv(roiBgr, this.PURE_MEMORY_HSV_MIN, this.PURE_MEMORY_HSV_MAX)
    }
}
