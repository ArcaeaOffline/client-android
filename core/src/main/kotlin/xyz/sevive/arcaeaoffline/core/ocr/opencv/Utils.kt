package xyz.sevive.arcaeaoffline.core.ocr.opencv

import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.imgcodecs.Imgcodecs
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

/**
 * Decodes image [bytes] into a newly allocated BGR [Mat]. Ownership passes to the caller.
 *
 * The bytes are copied into a native buffer before decoding, so the array becomes collectible as
 * soon as this returns instead of staying alive through the decode.
 *
 * A `MatOfByte(*bytes)` spread would copy the array once more on the Java heap.
 */
fun decodeBgrImage(bytes: ByteArray): Mat =
    Mat(1, bytes.size, CvType.CV_8U).use { buf ->
        buf.put(0, 0, bytes)
        Imgcodecs.imdecode(buf, Imgcodecs.IMREAD_COLOR)
    }
