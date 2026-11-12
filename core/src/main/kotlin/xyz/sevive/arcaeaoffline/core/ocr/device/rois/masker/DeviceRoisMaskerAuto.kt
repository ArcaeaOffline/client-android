package xyz.sevive.arcaeaoffline.core.ocr.device.rois.masker

import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import xyz.sevive.arcaeaoffline.core.ocr.opencv.use

/** Returns a newly allocated mask of [roiBgr] pixels inside the HSV range. Ownership passes to the caller. */
private fun maskBgrInHsv(
    roiBgr: Mat,
    hsvLower: Scalar,
    hsvUpper: Scalar,
): Mat {
    val masked = Mat()
    Mat().use { roiHsv ->
        Imgproc.cvtColor(roiBgr, roiHsv, Imgproc.COLOR_BGR2HSV)
        Core.inRange(roiHsv, hsvLower, hsvUpper, masked)
    }
    return masked
}

interface DeviceRoisMaskerAuto : DeviceRoisMasker

class DeviceRoisMaskerAutoT1 : DeviceRoisMaskerAuto {
    private val grayBgrLower = Scalar(50.0, 50.0, 50.0)
    private val grayBgrUpper = Scalar(160.0, 160.0, 160.0)

    private val whiteHsvLower = Scalar(0.0, 0.0, 180.0)
    private val whiteHsvUpper = Scalar(179.0, 255.0, 255.0)

    private val pstHsvLower = Scalar(100.0, 50.0, 80.0)
    private val pstHsvUpper = Scalar(100.0, 255.0, 255.0)

    private val prsHsvLower = Scalar(43.0, 40.0, 75.0)
    private val prsHsvUpper = Scalar(50.0, 155.0, 190.0)

    private val ftrHsvLower = Scalar(149.0, 30.0, 0.0)
    private val ftrHsvUpper = Scalar(155.0, 181.0, 150.0)

    private val bydHsvLower = Scalar(170.0, 50.0, 50.0)
    private val bydHsvUpper = Scalar(179.0, 210.0, 198.0)

    private val etrHsvMin = Scalar(130.0, 60.0, 80.0)
    private val etrHsvMax = Scalar(140.0, 145.0, 180.0)

    private val trackLostHsvLower = Scalar(170.0, 75.0, 90.0)
    private val trackLostHsvUpper = Scalar(175.0, 170.0, 160.0)

    private val trackCompleteHsvLower = Scalar(140.0, 0.0, 50.0)
    private val trackCompleteHsvUpper = Scalar(145.0, 50.0, 130.0)

    private val fullRecallHsvLower = Scalar(140.0, 60.0, 80.0)
    private val fullRecallHsvUpper = Scalar(150.0, 130.0, 145.0)

    private val pureMemoryHsvLower = Scalar(90.0, 70.0, 80.0)
    private val pureMemoryHsvUpper = Scalar(110.0, 200.0, 175.0)

    /** Returns a newly allocated 8UC1 mask with 255 where the BGR channel spread is at most [threshold]. */
    private fun channelSpreadMask(
        roiBgr: Mat,
        threshold: Double = 5.0,
    ): Mat {
        val b = Mat()
        val g = Mat()
        val r = Mat()
        val bgDiff = Mat()
        val brDiff = Mat()
        val grDiff = Mat()
        val maxDiff = Mat()
        val mask = Mat()
        try {
            Core.extractChannel(roiBgr, b, 0)
            Core.extractChannel(roiBgr, g, 1)
            Core.extractChannel(roiBgr, r, 2)
            // the largest pairwise diff is the spread (max − min), so this caps it at [threshold]
            Core.absdiff(b, g, bgDiff)
            Core.absdiff(b, r, brDiff)
            Core.absdiff(g, r, grDiff)
            Core.max(bgDiff, brDiff, maxDiff)
            Core.max(maxDiff, grDiff, maxDiff)
            Core.compare(maxDiff, Scalar(threshold), mask, Core.CMP_LE)
        } finally {
            listOf(b, g, r, bgDiff, brDiff, grDiff, maxDiff).forEach { it.release() }
        }
        return mask
    }

    /**
     * Masks gray pixels: BGR values within [grayBgrLower, grayBgrUpper] whose channel spread
     * (max − min) is at most 5. Pixels outside the spread are zeroed and the image eroded 2×2
     * before the range check.
     */
    fun gray(roiBgr: Mat): Mat {
        val masked = Mat()
        channelSpreadMask(roiBgr).use { mask ->
            Core.bitwise_and(roiBgr, roiBgr, masked, mask)
        }
        Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(2.0, 2.0)).use { kernel ->
            Imgproc.erode(masked, masked, kernel)
        }
        Core.inRange(masked, grayBgrLower, grayBgrUpper, masked)
        return masked
    }

    override fun pure(roiBgr: Mat): Mat = gray(roiBgr)

    override fun far(roiBgr: Mat): Mat = gray(roiBgr)

    override fun lost(roiBgr: Mat): Mat = gray(roiBgr)

    override fun maxRecall(roiBgr: Mat): Mat = gray(roiBgr)

    override fun score(roiBgr: Mat): Mat = maskBgrInHsv(roiBgr, whiteHsvLower, whiteHsvUpper)

    override fun ratingClassPst(roiBgr: Mat): Mat = maskBgrInHsv(roiBgr, pstHsvLower, pstHsvUpper)

    override fun ratingClassPrs(roiBgr: Mat): Mat = maskBgrInHsv(roiBgr, prsHsvLower, prsHsvUpper)

    override fun ratingClassFtr(roiBgr: Mat): Mat = maskBgrInHsv(roiBgr, ftrHsvLower, ftrHsvUpper)

    override fun ratingClassByd(roiBgr: Mat): Mat = maskBgrInHsv(roiBgr, bydHsvLower, bydHsvUpper)

    override fun ratingClassEtr(roiBgr: Mat): Mat = maskBgrInHsv(roiBgr, etrHsvMin, etrHsvMax)

    override fun clearStatusTrackLost(roiBgr: Mat): Mat = maskBgrInHsv(roiBgr, trackLostHsvLower, trackLostHsvUpper)

    override fun clearStatusTrackComplete(roiBgr: Mat): Mat = maskBgrInHsv(roiBgr, trackCompleteHsvLower, trackCompleteHsvUpper)

    override fun clearStatusFullRecall(roiBgr: Mat): Mat = maskBgrInHsv(roiBgr, fullRecallHsvLower, fullRecallHsvUpper)

    override fun clearStatusPureMemory(roiBgr: Mat): Mat = maskBgrInHsv(roiBgr, pureMemoryHsvLower, pureMemoryHsvUpper)
}

class DeviceRoisMaskerAutoT2 : DeviceRoisMaskerAuto {
    private val pflHsvLower = Scalar(0.0, 0.0, 248.0)
    private val pflHsvUpper = Scalar(179.0, 40.0, 255.0)

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

    private val etrHsvMin = Scalar(130.0, 60.0, 80.0)
    private val etrHsvMax = Scalar(140.0, 145.0, 180.0)

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

    private fun pfl(roiBgr: Mat): Mat = maskBgrInHsv(roiBgr, pflHsvLower, pflHsvUpper)

    override fun pure(roiBgr: Mat): Mat = pfl(roiBgr)

    override fun far(roiBgr: Mat): Mat = pfl(roiBgr)

    override fun lost(roiBgr: Mat): Mat = pfl(roiBgr)

    override fun score(roiBgr: Mat): Mat = maskBgrInHsv(roiBgr, scoreHsvLower, scoreHsvUpper)

    override fun ratingClassPst(roiBgr: Mat): Mat = maskBgrInHsv(roiBgr, pstHsvLower, pstHsvUpper)

    override fun ratingClassPrs(roiBgr: Mat): Mat = maskBgrInHsv(roiBgr, prsHsvLower, prsHsvUpper)

    override fun ratingClassFtr(roiBgr: Mat): Mat = maskBgrInHsv(roiBgr, ftrHsvLower, ftrHsvUpper)

    override fun ratingClassByd(roiBgr: Mat): Mat = maskBgrInHsv(roiBgr, bydHsvLower, bydHsvUpper)

    override fun ratingClassEtr(roiBgr: Mat): Mat = maskBgrInHsv(roiBgr, etrHsvMin, etrHsvMax)

    override fun maxRecall(roiBgr: Mat): Mat = maskBgrInHsv(roiBgr, maxRecallHsvLower, maxRecallHsvUpper)

    override fun clearStatusTrackLost(roiBgr: Mat): Mat = maskBgrInHsv(roiBgr, trackLostHsvLower, trackLostHsvUpper)

    override fun clearStatusTrackComplete(roiBgr: Mat): Mat = maskBgrInHsv(roiBgr, trackCompleteHsvLower, trackCompleteHsvUpper)

    override fun clearStatusFullRecall(roiBgr: Mat): Mat = maskBgrInHsv(roiBgr, fullRecallHsvLower, fullRecallHsvUpper)

    override fun clearStatusPureMemory(roiBgr: Mat): Mat = maskBgrInHsv(roiBgr, pureMemoryHsvLower, pureMemoryHsvUpper)
}
