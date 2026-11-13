package xyz.sevive.arcaeaoffline.core.ocr.device.rois

import co.touchlab.kermit.Logger
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import xyz.sevive.arcaeaoffline.core.ocr.device.rois.definition.DeviceRois
import xyz.sevive.arcaeaoffline.core.ocr.device.rois.definition.DeviceRoisAutoT1
import xyz.sevive.arcaeaoffline.core.ocr.device.rois.definition.DeviceRoisAutoT2
import xyz.sevive.arcaeaoffline.core.ocr.device.rois.extractor.doubleArrayRoundToRect
import xyz.sevive.arcaeaoffline.core.ocr.device.rois.masker.DeviceRoisMasker
import xyz.sevive.arcaeaoffline.core.ocr.device.rois.masker.DeviceRoisMaskerAutoT1
import xyz.sevive.arcaeaoffline.core.ocr.device.rois.masker.DeviceRoisMaskerAutoT2
import xyz.sevive.arcaeaoffline.core.ocr.opencv.use

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

    private val pureLabelHsvLower = Scalar(80.0, 60.0, 125.0)
    private val pureLabelHsvUpper = Scalar(110.0, 200.0, 225.0)

    private const val PURE_LABEL_NON_ZERO_RATIO_THRESHOLD = 0.08
    private const val FAR_LABEL_NON_ZERO_RATIO_THRESHOLD = 0.04
    private const val LOST_LABEL_NON_ZERO_RATIO_THRESHOLD = 0.055

    private val grayMasker = DeviceRoisMaskerAutoT1()

    private fun maskPureLabel(roiBgr: Mat): Mat {
        val roiMasked = Mat()
        Mat().use { roiHsv ->
            Imgproc.cvtColor(roiBgr, roiHsv, Imgproc.COLOR_BGR2HSV)
            Core.inRange(roiHsv, pureLabelHsvLower, pureLabelHsvUpper, roiMasked)
        }
        return roiMasked
    }

    @Suppress("ArrayInDataClass")
    private data class Interest(
        val rect: DoubleArray,
        val masker: (Mat) -> Mat,
        val nonZeroRatioThreshold: Double,
    )

    override fun confidence(imgBgr: Mat): Double {
        var matches = 0

        val rois = DeviceRoisAutoT1(imgBgr.width(), imgBgr.height())

        listOf(
            Interest(rois.pureLabel, ::maskPureLabel, PURE_LABEL_NON_ZERO_RATIO_THRESHOLD),
            Interest(rois.farLabel, grayMasker::gray, FAR_LABEL_NON_ZERO_RATIO_THRESHOLD),
            Interest(rois.lostLabel, grayMasker::gray, LOST_LABEL_NON_ZERO_RATIO_THRESHOLD),
        ).forEach { (rect, masker, threshold) ->
            imgBgr.submat(doubleArrayRoundToRect(rect)).use { labelBgr ->
                masker(labelBgr).use { labelMasked ->
                    val labelSize = labelMasked.size().height * labelMasked.size().width
                    if (Core.countNonZero(labelMasked) / labelSize >= threshold) matches += 1
                }
            }
        }

        return matches / INTEREST_POINTS
    }
}

private object T2Detector : DeviceRoisAutoSelectorDetector {
    override val result: DeviceRoisAutoSelectorResult = DeviceRoisAutoSelectorResult.T2

    private const val INTEREST_POINTS = 3.0

    private val pureLabelHsvLower = Scalar(110.0, 25.0, 90.0)
    private val pureLabelHsvUpper = Scalar(160.0, 150.0, 230.0)
    private val farLabelHsvLower = Scalar(5.0, 25.0, 120.0)
    private val farLabelHsvUpper = Scalar(20.0, 100.0, 240.0)
    private val lostLabelHsvLower = Scalar(160.0, 5.0, 190.0)
    private val lostLabelHsvUpper = Scalar(179.0, 60.0, 255.0)

    private const val PFL_LABEL_NON_ZERO_RATIO_THRESHOLD = 0.3

    @Suppress("ArrayInDataClass")
    private data class Interest(
        val rect: DoubleArray,
        val hsvLower: Scalar,
        val hsvUpper: Scalar,
    )

    override fun confidence(imgBgr: Mat): Double {
        var matches = 0

        val rois = DeviceRoisAutoT2(imgBgr.width(), imgBgr.height())

        listOf(
            Interest(rois.pureLabel, pureLabelHsvLower, pureLabelHsvUpper),
            Interest(rois.farLabel, farLabelHsvLower, farLabelHsvUpper),
            Interest(rois.lostLabel, lostLabelHsvLower, lostLabelHsvUpper),
        ).forEach { (rect, hsvLower, hsvUpper) ->
            imgBgr.submat(doubleArrayRoundToRect(rect)).use { labelBgr ->
                Mat().use { labelHsv ->
                    Imgproc.cvtColor(labelBgr, labelHsv, Imgproc.COLOR_BGR2HSV)
                    Mat().use { labelMasked ->
                        Core.inRange(labelHsv, hsvLower, hsvUpper, labelMasked)
                        val labelSize = labelMasked.size().height * labelMasked.size().width
                        if (Core.countNonZero(labelMasked) / labelSize >= PFL_LABEL_NON_ZERO_RATIO_THRESHOLD) {
                            matches += 1
                        }
                    }
                }
            }
        }

        return matches / INTEREST_POINTS
    }
}

object DeviceRoisAutoSelector {
    private const val LOG_TAG = "DeviceRoisAutoSelector"
    private val logger = Logger.withTag(LOG_TAG)

    @Suppress("MemberVisibilityCanBePrivate")
    val selectors = mutableListOf(T1Detector, T2Detector)

    private fun logDetectorResults(result: Map<DeviceRoisAutoSelectorDetector, Double>) {
        val resultsSeparated =
            result
                .map {
                    "\t${it.key.javaClass.simpleName} -> ${it.key.result}: ${it.value}"
                }.joinToString("\n")
        logger.d { "Detector results:\n$resultsSeparated" }
    }

    fun select(
        imgBgr: Mat,
        fallback: DeviceRoisAutoSelectorResult? = null,
    ): DeviceRoisAutoSelectorResult {
        val results = selectors.associateWith { it.confidence(imgBgr) }
        logDetectorResults(results)

        if (results.all { it.value == 0.0 }) return fallback ?: DeviceRoisAutoSelectorResult.UNKNOWN
        return results.maxBy { it.value }.key.result
    }

    /**
     * Creates the rois/masker pair matching the selector [result].
     *
     * @throws IllegalArgumentException when [result] is [DeviceRoisAutoSelectorResult.UNKNOWN].
     */
    fun createRoisAndMasker(
        result: DeviceRoisAutoSelectorResult,
        w: Int,
        h: Int,
    ): Pair<DeviceRois, DeviceRoisMasker> =
        when (result) {
            DeviceRoisAutoSelectorResult.T1 -> {
                DeviceRoisAutoT1(w, h) to DeviceRoisMaskerAutoT1()
            }

            DeviceRoisAutoSelectorResult.T2 -> {
                DeviceRoisAutoT2(w, h) to DeviceRoisMaskerAutoT2()
            }

            DeviceRoisAutoSelectorResult.UNKNOWN -> {
                throw IllegalArgumentException("No rois/masker pair for $result")
            }
        }
}
