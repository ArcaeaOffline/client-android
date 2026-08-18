package xyz.sevive.arcaeaoffline.core.ocr

import org.opencv.core.Mat
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
