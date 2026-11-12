package xyz.sevive.arcaeaoffline.core.ocr.device.rois.masker

import org.opencv.core.Mat

/** Every method returns a newly allocated Mat; ownership passes to the caller. */
interface DeviceRoisMasker {
    fun pure(roiBgr: Mat): Mat

    fun far(roiBgr: Mat): Mat

    fun lost(roiBgr: Mat): Mat

    fun score(roiBgr: Mat): Mat

    fun ratingClassPst(roiBgr: Mat): Mat

    fun ratingClassPrs(roiBgr: Mat): Mat

    fun ratingClassFtr(roiBgr: Mat): Mat

    fun ratingClassByd(roiBgr: Mat): Mat

    fun ratingClassEtr(roiBgr: Mat): Mat

    fun maxRecall(roiBgr: Mat): Mat

    fun clearStatusTrackLost(roiBgr: Mat): Mat

    fun clearStatusTrackComplete(roiBgr: Mat): Mat

    fun clearStatusFullRecall(roiBgr: Mat): Mat

    fun clearStatusPureMemory(roiBgr: Mat): Mat

    /**
     * Votes for a rating class by the non-zero pixel count of [roiBgr] masked under each
     * class's HSV range, with a single BGR→HSV conversion.
     *
     * Index i is the vote count for `ArcaeaRatingClass.fromInt(i)`.
     */
    fun ratingClassVotes(roiBgr: Mat): IntArray

    /**
     * Votes for a clear status by the non-zero pixel count of [roiBgr] masked under each
     * status's HSV range, with a single BGR→HSV conversion.
     *
     * Index order is part of the persisted result contract:
     * * 0 = [xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultClearStatus.TRACK_LOST]
     * * 1 = [xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultClearStatus.CLEARED]
     * * 2 = [xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultClearStatus.FULL_RECALL]
     * * 3 = [xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultClearStatus.PURE_MEMORY]
     */
    fun clearStatusVotes(roiBgr: Mat): IntArray
}
