package xyz.sevive.arcaeaoffline.core.ocr.opencv

import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * OpenCV Mats are backed by native memory and only reclaimed by GC
 * finalization otherwise, which delays reclamation unpredictably.
 */
@OptIn(ExperimentalContracts::class)
inline fun <T> Mat.use(block: (Mat) -> T): T {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    return try {
        block(this)
    } finally {
        release()
    }
}

/** Returns a newly allocated mask of [roiBgr] pixels inside the HSV range. Ownership passes to the caller. */
internal fun maskBgrInHsv(
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
