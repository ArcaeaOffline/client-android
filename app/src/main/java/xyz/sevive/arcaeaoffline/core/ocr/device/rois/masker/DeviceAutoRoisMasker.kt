package xyz.sevive.arcaeaoffline.core.ocr.device.rois.masker

import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc

interface DeviceAutoRoisMasker : DeviceRoisMasker

//class DeviceAutoRoisMaskerT1 : DeviceAutoRoisMasker {
//    val GRAY_BGR_MIN = Scalar(50.0, 50.0, 50.0)
//    val GRAY_BGR_MAX = Scalar(160.0, 160.0, 160.0)
//
//    val WHITE_HSV_MIN = Scalar(0.0, 0.0, 240.0)
//    val WHITE_HSV_MAX = Scalar(179.0, 10.0, 255.0)
//
//    val PST_HSV_MIN = Scalar(100.0, 50.0, 80.0)
//    val PST_HSV_MAX = Scalar(100.0, 255.0, 255.0)
//
//    val PRS_HSV_MIN = Scalar(43.0, 40.0, 75.0)
//    val PRS_HSV_MAX = Scalar(50.0, 155.0, 190.0)
//
//    val FTR_HSV_MIN = Scalar(149.0, 30.0, 0.0)
//    val FTR_HSV_MAX = Scalar(155.0, 181.0, 150.0)
//
//    val BYD_HSV_MIN = Scalar(170.0, 50.0, 50.0)
//    val BYD_HSV_MAX = Scalar(179.0, 210.0, 198.0)
//
//    val TRACK_LOST_HSV_MIN = Scalar(170.0, 75.0, 90.0)
//    val TRACK_LOST_HSV_MAX = Scalar(175.0, 170.0, 160.0)
//
//    val TRACK_COMPLETE_HSV_MIN = Scalar(140.0, 0.0, 50.0)
//    val TRACK_COMPLETE_HSV_MAX = Scalar(145.0, 50.0, 130.0)
//
//    val FULL_RECALL_HSV_MIN = Scalar(140.0, 60.0, 80.0)
//    val FULL_RECALL_HSV_MAX = Scalar(150.0, 130.0, 145.0)
//
//    val PURE_MEMORY_HSV_MIN = Scalar(90.0, 70.0, 80.0)
//    val PURE_MEMORY_HSV_MAX = Scalar(110.0, 200.0, 175.0)
//
//    fun gray(roiBgr: Mat) {
//
//    }
//}

class DeviceAutoRoisMaskerT2 : DeviceAutoRoisMasker {
    private val pflHsvLower = Scalar(0.0, 0.0, 245.0)
    private val pflHsvUpper = Scalar(179.0, 30.0, 255.0)

//    private val pureSupplementHsvLower = Scalar(120.0, 0.0, 180.0)
//    private val pureSupplementHsvUpper = Scalar(135.0, 30.0, 200.0)
//
//    private val farSupplementHsvLower = Scalar(0.0, 0.0, 160.0)
//    private val farSupplementHsvUpper = Scalar(30.0, 90.0, 225.0)
//
//    private val lostSupplementHsvLower = Scalar(145.0, 0.0, 130.0)
//    private val lostSupplementHsvUpper = Scalar(175.0, 50.0, 155.0)

//    private val pureSupplementHsvLower = Scalar(0.0, 0.0, 0.0)
//    private val pureSupplementHsvUpper = Scalar(0.0, 0.0, 0.0)
//
//    private val farSupplementHsvLower = Scalar(0.0, 0.0, 0.0)
//    private val farSupplementHsvUpper = Scalar(0.0, 0.0, 0.0)
//
//    private val lostSupplementHsvLower = Scalar(0.0, 0.0, 0.0)
//    private val lostSupplementHsvUpper = Scalar(0.0, 0.0, 0.0)

    private val scoreHsvLower = Scalar(0.0, 0.0, 180.0)
    private val scoreHsvUpper = Scalar(179.0, 255.0, 255.0)

    private val pstHsvLower = Scalar(100.0, 50.0, 80.0)
    private val pstHsvUpper = Scalar(100.0, 255.0, 255.0)

    private val prsHsvLower = Scalar(43.0, 40.0, 75.0)
    private val prsHsvUpper = Scalar(50.0, 155.0, 190.0)

    private val ftrHsvLower = Scalar(149.0, 30.0, 0.0)
    private val ftrHsvUpper = Scalar(155.0, 181.0, 150.0)

    private val bydHsvLower = Scalar(170.0, 50.0, 50.0)
    private val bydHsvUpper = Scalar(179.0, 210.0, 198.0)

    private val maxRecallHsvLower = Scalar(125.0, 0.0, 0.0)
    private val maxRecallHsvUpper = Scalar(145.0, 100.0, 150.0)

    private val trackLostHsvLower = Scalar(170.0, 75.0, 90.0)
    private val trackLostHsvUpper = Scalar(175.0, 170.0, 160.0)

    private val trackCompleteHsvLower = Scalar(140.0, 0.0, 50.0)
    private val trackCompleteHsvUpper = Scalar(145.0, 50.0, 130.0)

    private val fullRecallHsvLower = Scalar(140.0, 60.0, 80.0)
    private val fullRecallHsvUpper = Scalar(150.0, 130.0, 145.0)

    private val pureMemoryHsvLower = Scalar(90.0, 70.0, 80.0)
    private val pureMemoryHsvUpper = Scalar(110.0, 200.0, 175.0)

    private fun maskHsv(roiBgr: Mat, hsvLower: Scalar, hsvUpper: Scalar): Mat {
        val roiHsv = Mat()
        val dst = Mat()
        Imgproc.cvtColor(roiBgr, roiHsv, Imgproc.COLOR_BGR2HSV)
        Core.inRange(roiHsv, hsvLower, hsvUpper, dst)
        return dst
    }

//    private fun pfl(roiBgr: Mat, supplementLower: Scalar, supplementUpper: Scalar): Mat {
//        val result = Mat()
//
//        val common = maskHsv(roiBgr, this.pflHsvLower, this.pflHsvUpper)
//        val supplement = maskHsv(roiBgr, supplementLower, supplementUpper)
//        Core.bitwise_or(common, supplement, result)
//
//        return result
//    }

    private fun pfl(roiBgr: Mat): Mat {
        return this.maskHsv(roiBgr, this.pflHsvLower, this.pflHsvUpper)
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
        return this.maskHsv(roiBgr, this.scoreHsvLower, this.scoreHsvUpper)
    }

    override fun ratingClassPst(roiBgr: Mat): Mat {
        return this.maskHsv(roiBgr, this.pstHsvLower, this.pstHsvUpper)
    }

    override fun ratingClassPrs(roiBgr: Mat): Mat {
        return this.maskHsv(roiBgr, this.prsHsvLower, this.prsHsvUpper)
    }

    override fun ratingClassFtr(roiBgr: Mat): Mat {
        return this.maskHsv(roiBgr, this.ftrHsvLower, this.ftrHsvUpper)
    }

    override fun ratingClassByd(roiBgr: Mat): Mat {
        return this.maskHsv(roiBgr, this.bydHsvLower, this.bydHsvUpper)
    }

    override fun maxRecall(roiBgr: Mat): Mat {
        return this.maskHsv(roiBgr, this.maxRecallHsvLower, this.maxRecallHsvUpper)
    }

    override fun clearStatusTrackLost(roiBgr: Mat): Mat {
        return this.maskHsv(roiBgr, this.trackLostHsvLower, this.trackLostHsvUpper)
    }

    override fun clearStatusTrackComplete(roiBgr: Mat): Mat {
        return this.maskHsv(roiBgr, this.trackCompleteHsvLower, this.trackCompleteHsvUpper)
    }

    override fun clearStatusFullRecall(roiBgr: Mat): Mat {
        return this.maskHsv(roiBgr, this.fullRecallHsvLower, this.fullRecallHsvUpper)
    }

    override fun clearStatusPureMemory(roiBgr: Mat): Mat {
        return this.maskHsv(roiBgr, this.pureMemoryHsvLower, this.pureMemoryHsvUpper)
    }
}
