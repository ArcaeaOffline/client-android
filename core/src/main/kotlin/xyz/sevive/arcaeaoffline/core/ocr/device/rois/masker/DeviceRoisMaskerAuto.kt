package xyz.sevive.arcaeaoffline.core.ocr.device.rois.masker

import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Scalar
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

/**
 * Counts non-zero pixels of [roiBgr] masked under each HSV ranges in order with a single
 * BGR -> HSV conversion. `maxOf` is used for subranges.
 */
private fun hsvVote(
    roiBgr: Mat,
    ranges: List<List<Pair<Scalar, Scalar>>>,
): IntArray {
    val votes = IntArray(ranges.size)
    Mat().use { roiHsv ->
        Imgproc.cvtColor(roiBgr, roiHsv, Imgproc.COLOR_BGR2HSV)
        Mat().use { mask ->
            ranges.forEachIndexed { index, subranges ->
                subranges.forEach { (lower, upper) ->
                    Core.inRange(roiHsv, lower, upper, mask)
                    votes[index] = maxOf(Core.countNonZero(mask), votes[index])
                }
            }
        }
    }
    return votes
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

    private val etrHsvLower = Scalar(130.0, 60.0, 80.0)
    private val etrHsvUpper = Scalar(140.0, 145.0, 180.0)

    private val insHsvLower = Scalar(110.0, 115.0, 80.0)
    private val insHsvUpper = Scalar(120.0, 180.0, 150.0)

    private val trackLostHsvLower = Scalar(170.0, 75.0, 90.0)
    private val trackLostHsvUpper = Scalar(175.0, 170.0, 160.0)

    private val trackCompleteHsvLower = Scalar(140.0, 0.0, 50.0)
    private val trackCompleteHsvUpper = Scalar(145.0, 50.0, 130.0)

    private val fullRecallHsvLower = Scalar(140.0, 60.0, 80.0)
    private val fullRecallHsvUpper = Scalar(150.0, 130.0, 145.0)

    private val pureMemoryHsvLower = Scalar(90.0, 70.0, 80.0)
    private val pureMemoryHsvUpper = Scalar(110.0, 200.0, 175.0)

    private val ratingClassRanges =
        listOf(
            listOf(pstHsvLower to pstHsvUpper),
            listOf(prsHsvLower to prsHsvUpper),
            listOf(ftrHsvLower to ftrHsvUpper),
            // T1 does not actually have INS difficulty
            listOf(bydHsvLower to bydHsvUpper),
            listOf(etrHsvLower to etrHsvUpper),
        )

    private val clearStatusRanges =
        listOf(
            listOf(trackLostHsvLower to trackLostHsvUpper),
            listOf(trackCompleteHsvLower to trackCompleteHsvUpper),
            listOf(fullRecallHsvLower to fullRecallHsvUpper),
            listOf(pureMemoryHsvLower to pureMemoryHsvUpper),
        )

    override fun pure(roiBgr: Mat): Mat = RgbGrayMasker.gray(roiBgr, grayBgrLower, grayBgrUpper)

    override fun far(roiBgr: Mat): Mat = RgbGrayMasker.gray(roiBgr, grayBgrLower, grayBgrUpper)

    override fun lost(roiBgr: Mat): Mat = RgbGrayMasker.gray(roiBgr, grayBgrLower, grayBgrUpper)

    override fun maxRecall(roiBgr: Mat): Mat = RgbGrayMasker.gray(roiBgr, grayBgrLower, grayBgrUpper)

    override fun score(roiBgr: Mat): Mat = maskBgrInHsv(roiBgr, whiteHsvLower, whiteHsvUpper)

    override fun ratingClassPst(roiBgr: Mat): Mat = maskBgrInHsv(roiBgr, pstHsvLower, pstHsvUpper)

    override fun ratingClassPrs(roiBgr: Mat): Mat = maskBgrInHsv(roiBgr, prsHsvLower, prsHsvUpper)

    override fun ratingClassFtr(roiBgr: Mat): Mat = maskBgrInHsv(roiBgr, ftrHsvLower, ftrHsvUpper)

    override fun ratingClassByd(roiBgr: Mat): Mat = maskBgrInHsv(roiBgr, bydHsvLower, bydHsvUpper)

    override fun ratingClassEtr(roiBgr: Mat): Mat = maskBgrInHsv(roiBgr, etrHsvLower, etrHsvUpper)

    override fun ratingClassIns(roiBgr: Mat): Mat = maskBgrInHsv(roiBgr, insHsvLower, insHsvUpper)

    override fun clearStatusTrackLost(roiBgr: Mat): Mat = maskBgrInHsv(roiBgr, trackLostHsvLower, trackLostHsvUpper)

    override fun clearStatusTrackComplete(roiBgr: Mat): Mat = maskBgrInHsv(roiBgr, trackCompleteHsvLower, trackCompleteHsvUpper)

    override fun clearStatusFullRecall(roiBgr: Mat): Mat = maskBgrInHsv(roiBgr, fullRecallHsvLower, fullRecallHsvUpper)

    override fun clearStatusPureMemory(roiBgr: Mat): Mat = maskBgrInHsv(roiBgr, pureMemoryHsvLower, pureMemoryHsvUpper)

    override fun ratingClassVotes(roiBgr: Mat): IntArray = hsvVote(roiBgr, ratingClassRanges)

    override fun clearStatusVotes(roiBgr: Mat): IntArray = hsvVote(roiBgr, clearStatusRanges)
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

    private val etrHsvLower = Scalar(130.0, 60.0, 80.0)
    private val etrHsvUpper = Scalar(140.0, 145.0, 180.0)

    private val insHsvLower = Scalar(110.0, 115.0, 80.0)
    private val insHsvUpper = Scalar(120.0, 180.0, 150.0)

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

    private val ratingClassRanges =
        listOf(
            listOf(pstHsvLower to pstHsvUpper),
            listOf(prsHsvLower to prsHsvUpper),
            listOf(ftrHsvLower to ftrHsvUpper),
            listOf(bydHsvLower to bydHsvUpper, insHsvLower to insHsvUpper),
            listOf(etrHsvLower to etrHsvUpper),
        )

    private val clearStatusRanges =
        listOf(
            listOf(trackLostHsvLower to trackLostHsvUpper),
            listOf(trackCompleteHsvLower to trackCompleteHsvUpper),
            listOf(fullRecallHsvLower to fullRecallHsvUpper),
            listOf(pureMemoryHsvLower to pureMemoryHsvUpper),
        )

    private fun pfl(roiBgr: Mat): Mat = maskBgrInHsv(roiBgr, pflHsvLower, pflHsvUpper)

    override fun pure(roiBgr: Mat): Mat = pfl(roiBgr)

    override fun far(roiBgr: Mat): Mat = pfl(roiBgr)

    override fun lost(roiBgr: Mat): Mat = pfl(roiBgr)

    override fun score(roiBgr: Mat): Mat = maskBgrInHsv(roiBgr, scoreHsvLower, scoreHsvUpper)

    override fun ratingClassPst(roiBgr: Mat): Mat = maskBgrInHsv(roiBgr, pstHsvLower, pstHsvUpper)

    override fun ratingClassPrs(roiBgr: Mat): Mat = maskBgrInHsv(roiBgr, prsHsvLower, prsHsvUpper)

    override fun ratingClassFtr(roiBgr: Mat): Mat = maskBgrInHsv(roiBgr, ftrHsvLower, ftrHsvUpper)

    override fun ratingClassByd(roiBgr: Mat): Mat = maskBgrInHsv(roiBgr, bydHsvLower, bydHsvUpper)

    override fun ratingClassEtr(roiBgr: Mat): Mat = maskBgrInHsv(roiBgr, etrHsvLower, etrHsvUpper)

    override fun ratingClassIns(roiBgr: Mat): Mat = maskBgrInHsv(roiBgr, insHsvLower, insHsvUpper)

    override fun maxRecall(roiBgr: Mat): Mat = maskBgrInHsv(roiBgr, maxRecallHsvLower, maxRecallHsvUpper)

    override fun clearStatusTrackLost(roiBgr: Mat): Mat = maskBgrInHsv(roiBgr, trackLostHsvLower, trackLostHsvUpper)

    override fun clearStatusTrackComplete(roiBgr: Mat): Mat = maskBgrInHsv(roiBgr, trackCompleteHsvLower, trackCompleteHsvUpper)

    override fun clearStatusFullRecall(roiBgr: Mat): Mat = maskBgrInHsv(roiBgr, fullRecallHsvLower, fullRecallHsvUpper)

    override fun clearStatusPureMemory(roiBgr: Mat): Mat = maskBgrInHsv(roiBgr, pureMemoryHsvLower, pureMemoryHsvUpper)

    override fun ratingClassVotes(roiBgr: Mat): IntArray = hsvVote(roiBgr, ratingClassRanges)

    override fun clearStatusVotes(roiBgr: Mat): IntArray = hsvVote(roiBgr, clearStatusRanges)
}
