package xyz.sevive.arcaeaoffline.core.ocr.device.rois

import android.util.Log
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import xyz.sevive.arcaeaoffline.core.ocr.device.rois.definition.DeviceRoisAutoT1
import xyz.sevive.arcaeaoffline.core.ocr.device.rois.definition.DeviceRoisAutoT2
import xyz.sevive.arcaeaoffline.core.ocr.device.rois.extractor.doubleArrayRoundToRect
import xyz.sevive.arcaeaoffline.core.ocr.device.rois.masker.DeviceRoisMaskerAutoT1


enum class DeviceRoisAutoSelectorResult { T1, T2, UNKNOWN }

interface DeviceRoisAutoSelectorDetector {
    val result: DeviceRoisAutoSelectorResult

    /**
     * Analyze the [imgBgr], then return the confidence of the [imgBgr].
     *
     * For example, if the detector has 3 interest points:
     *
     * * PURE label should be blue,
     * * FAR label should be gray,
     * * LOST label should be gray
     *
     * and if 2 of them matches, then the return value would be `0.66`.
     */
    fun confidence(imgBgr: Mat): Double
}

private object T1Detector : DeviceRoisAutoSelectorDetector {
    override val result: DeviceRoisAutoSelectorResult = DeviceRoisAutoSelectorResult.T1

    private const val INTEREST_POINTS = 3.0

    private const val PFL_LABEL_WIDTH = 85
    private val pureLabelHsvLower = Scalar(80.0, 60.0, 125.0)
    private val pureLabelHsvUpper = Scalar(110.0, 200.0, 225.0)

    private const val PURE_LABEL_NON_ZERO_RATIO_THRESHOLD = 0.08
    private const val FAR_LABEL_NON_ZERO_RATIO_THRESHOLD = 0.04
    private const val LOST_LABEL_NON_ZERO_RATIO_THRESHOLD = 0.055

    private val masker = DeviceRoisMaskerAutoT1()

    private fun getPflLabelRectFromPflRoiRect(array: DoubleArray, labelWidth: Double): Rect {
        return doubleArrayRoundToRect(
            doubleArrayOf(array[0] - labelWidth, array[1], labelWidth, array[3])
        )
    }

    private fun getPflLabelBgr(imgBgr: Mat, roiRect: DoubleArray): Mat {
        val rois = DeviceRoisAutoT1(imgBgr.width(), imgBgr.height())
        val pflLabelWidth = PFL_LABEL_WIDTH * rois.factor

        val labelRect = getPflLabelRectFromPflRoiRect(roiRect, pflLabelWidth)
        return imgBgr.submat(labelRect).clone()
    }

    private fun maskPureLabel(roiBgr: Mat): Mat {
        val roiHsv = Mat()
        Imgproc.cvtColor(roiBgr, roiHsv, Imgproc.COLOR_BGR2HSV)
        val roiMasked = Mat()
        Core.inRange(roiHsv, pureLabelHsvLower, pureLabelHsvUpper, roiMasked)
        return roiMasked
    }

    private fun maskFarLostLabel(roiBgr: Mat): Mat = masker.gray(roiBgr)

    private fun pflLabelMatches(labelMasked: Mat, nonZeroRatioThreshold: Double): Boolean {
        val labelSize = labelMasked.size().height * labelMasked.size().width
        val nonZeroRatio = Core.countNonZero(labelMasked) / labelSize
        return nonZeroRatio >= nonZeroRatioThreshold
    }

    @Suppress("ArrayInDataClass")
    private data class PflLabelInterest(
        val roiRect: DoubleArray, val masker: (Mat) -> Mat, val nonZeroRatioThreshold: Double
    )

    override fun confidence(imgBgr: Mat): Double {
        var matches = 0

        val rois = DeviceRoisAutoT1(imgBgr.width(), imgBgr.height())

        listOf(
            PflLabelInterest(rois.pure, ::maskPureLabel, PURE_LABEL_NON_ZERO_RATIO_THRESHOLD),
            PflLabelInterest(rois.far, ::maskFarLostLabel, FAR_LABEL_NON_ZERO_RATIO_THRESHOLD),
            PflLabelInterest(rois.lost, ::maskFarLostLabel, LOST_LABEL_NON_ZERO_RATIO_THRESHOLD),
        ).forEach {
            val labelBgr = getPflLabelBgr(imgBgr, it.roiRect)
            val labelMasked = it.masker(labelBgr)
            if (pflLabelMatches(labelMasked, it.nonZeroRatioThreshold)) matches += 1
        }

        return matches / INTEREST_POINTS
    }
}

private object T2Detector : DeviceRoisAutoSelectorDetector {
    override val result: DeviceRoisAutoSelectorResult = DeviceRoisAutoSelectorResult.T2

    private const val INTEREST_POINTS = 3.0

    private const val PFL_LABEL_WIDTH = 180
    private val pureLabelHsvLower = Scalar(110.0, 25.0, 90.0)
    private val pureLabelHsvUpper = Scalar(160.0, 150.0, 230.0)
    private val farLabelHsvLower = Scalar(5.0, 25.0, 120.0)
    private val farLabelHsvUpper = Scalar(20.0, 100.0, 240.0)
    private val lostLabelHsvLower = Scalar(160.0, 5.0, 190.0)
    private val lostLabelHsvUpper = Scalar(179.0, 60.0, 255.0)

    private const val PFL_LABEL_NON_ZERO_RATIO_THRESHOLD = 0.3

    private fun getPflLabelRectFromPflRoiRect(array: DoubleArray, labelWidth: Double): Rect {
        return doubleArrayRoundToRect(
            doubleArrayOf(array[0] - labelWidth, array[1], labelWidth, array[3])
        )
    }

    private fun pflLabelMatches(
        imgBgr: Mat,
        roiRect: DoubleArray,
        hsvLower: Scalar,
        hsvUpper: Scalar,
    ): Boolean {
        val rois = DeviceRoisAutoT2(imgBgr.width(), imgBgr.height())
        val pflLabelWidth = PFL_LABEL_WIDTH * rois.factor
        val labelRect = getPflLabelRectFromPflRoiRect(roiRect, pflLabelWidth)

        val labelBgr = imgBgr.submat(labelRect).clone()
        val labelHsv = Mat()
        Imgproc.cvtColor(labelBgr, labelHsv, Imgproc.COLOR_BGR2HSV)
        val labelMasked = Mat()
        Core.inRange(labelHsv, hsvLower, hsvUpper, labelMasked)

        val labelSize = labelMasked.size().height * labelMasked.size().width
        val nonZeroRatio = Core.countNonZero(labelMasked) / labelSize
        return nonZeroRatio >= PFL_LABEL_NON_ZERO_RATIO_THRESHOLD
    }

    @Suppress("ArrayInDataClass")
    private data class PflLabelInterest(
        val roiRect: DoubleArray, val hsvLower: Scalar, val hsvUpper: Scalar
    )

    override fun confidence(imgBgr: Mat): Double {
        var matches = 0
        val rois = DeviceRoisAutoT2(imgBgr.width(), imgBgr.height())

        listOf(
            PflLabelInterest(rois.pure, pureLabelHsvLower, pureLabelHsvUpper),
            PflLabelInterest(rois.far, farLabelHsvLower, farLabelHsvUpper),
            PflLabelInterest(rois.lost, lostLabelHsvLower, lostLabelHsvUpper),
        ).forEach {
            if (pflLabelMatches(imgBgr, it.roiRect, it.hsvLower, it.hsvUpper)) matches += 1
        }

        return matches / INTEREST_POINTS
    }
}

object DeviceRoisAutoSelector {
    private const val LOG_TAG = "DeviceRoisAutoSelector"

    @Suppress("MemberVisibilityCanBePrivate")
    val selectors = mutableListOf(T1Detector, T2Detector)

    private fun logDetectorResults(result: Map<DeviceRoisAutoSelectorDetector, Double>) {
        val resultsSeparated = result.map {
            "\t${it.key.javaClass.simpleName} -> ${it.key.result}: ${it.value}"
        }.joinToString("\n")
        Log.d(LOG_TAG, "Detector results:\n$resultsSeparated")
    }

    fun select(
        imgBgr: Mat, fallback: DeviceRoisAutoSelectorResult? = null
    ): DeviceRoisAutoSelectorResult {
        val results = selectors.associateWith { it.confidence(imgBgr) }
        logDetectorResults(results)

        if (results.all { it.value == 0.0 }) return fallback ?: DeviceRoisAutoSelectorResult.UNKNOWN
        return results.maxBy { it.value }.key.result
    }
}
