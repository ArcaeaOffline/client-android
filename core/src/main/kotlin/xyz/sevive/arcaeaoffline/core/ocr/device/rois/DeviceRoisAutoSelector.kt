package xyz.sevive.arcaeaoffline.core.ocr.device.rois

import co.touchlab.kermit.Logger
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Rect
import org.opencv.core.Scalar
import xyz.sevive.arcaeaoffline.core.ocr.device.rois.definition.DeviceRois
import xyz.sevive.arcaeaoffline.core.ocr.device.rois.definition.DeviceRoisAutoT1
import xyz.sevive.arcaeaoffline.core.ocr.device.rois.definition.DeviceRoisAutoT2
import xyz.sevive.arcaeaoffline.core.ocr.device.rois.extractor.doubleArrayRoundToRect
import xyz.sevive.arcaeaoffline.core.ocr.device.rois.masker.DeviceRoisMasker
import xyz.sevive.arcaeaoffline.core.ocr.device.rois.masker.DeviceRoisMaskerAutoT1
import xyz.sevive.arcaeaoffline.core.ocr.device.rois.masker.DeviceRoisMaskerAutoT2
import xyz.sevive.arcaeaoffline.core.ocr.device.rois.masker.RgbGrayMasker
import xyz.sevive.arcaeaoffline.core.ocr.opencv.maskBgrInHsv
import xyz.sevive.arcaeaoffline.core.ocr.opencv.use
import kotlin.math.roundToInt

enum class DeviceRoisAutoSelectorResult { T1, T2, UNKNOWN }

/** One interest point check: the non-zero pixel ratio of its masked label ROI against the threshold. */
data class LabelMatch(
    val name: String,
    val ratio: Double,
    val threshold: Double,
) {
    val matched: Boolean get() = ratio >= threshold
}

data class DetectionReport(
    val labels: List<LabelMatch>,
) {
    /** Fraction of [labels] that matched, in the `[0, 1]` range. */
    val confidence: Double get() = labels.count { it.matched } / labels.size.toDouble()
}

interface DeviceRoisAutoSelectorDetector {
    val result: DeviceRoisAutoSelectorResult

    /**
     * Analyze the [imgBgr] and report each interest point's mask ratio against its threshold.
     *
     * For example, if the detector has 3 interest points and 2 of them match, the report's
     * [DetectionReport.confidence] would be `0.66`.
     */
    fun analyze(imgBgr: Mat): DetectionReport
}

@Suppress("ArrayInDataClass")
private data class Interest(
    val name: String,
    val rect: DoubleArray,
    val masker: (Mat) -> Mat,
    val nonZeroRatioThreshold: Double,
)

private fun analyzeLabels(
    imgBgr: Mat,
    interests: List<Interest>,
): List<LabelMatch> =
    interests.map { (name, rect, masker, threshold) ->
        imgBgr.submat(doubleArrayRoundToRect(rect)).use { labelBgr ->
            masker(labelBgr).use { labelMasked ->
                val labelSize = labelMasked.total().toDouble()
                LabelMatch(name, Core.countNonZero(labelMasked) / labelSize, threshold)
            }
        }
    }

private data class StripMatch(
    val purple: LabelMatch,
    val gray: LabelMatch,
)

/**
 * The top-left background strip check that gates label detection: purple is the regular Arcaea skin,
 * gray (BGR 10..40) is the Arknights collab skin, and any other background cannot be an Arcaea
 * result screenshot. The strip anchors to T2 geometry and holds for T1 screenshots as well.
 */
private object StripProbe {
    private const val HEIGHT_OFFSET = -510
    private const val WIDTH_OFFSET = -250
    private const val HEIGHT = 165

    private val purpleHsvLower = Scalar(110.0, 45.0, 75.0)
    private val purpleHsvUpper = Scalar(140.0, 150.0, 175.0)
    private const val PURPLE_NON_ZERO_RATIO_THRESHOLD = 0.65

    private val grayBgrLower = Scalar(10.0, 10.0, 10.0)
    private val grayBgrUpper = Scalar(40.0, 40.0, 40.0)
    private const val GRAY_NON_ZERO_RATIO_THRESHOLD = 0.5

    fun analyze(imgBgr: Mat): StripMatch {
        val rois = DeviceRoisAutoT2(imgBgr.width(), imgBgr.height())

        val y = (rois.layoutAreaHMid + HEIGHT_OFFSET * rois.factor).roundToInt()
        val w = (rois.wMid + WIDTH_OFFSET * rois.factor).roundToInt()
        val h = (HEIGHT * rois.factor).roundToInt()

        return imgBgr.submat(Rect(0, y, w, h)).use { stripBgr ->
            val stripSize = stripBgr.total().toDouble()
            maskBgrInHsv(stripBgr, purpleHsvLower, purpleHsvUpper).use { purpleMask ->
                RgbGrayMasker.gray(stripBgr, grayBgrLower, grayBgrUpper).use { grayMask ->
                    StripMatch(
                        LabelMatch("purple", Core.countNonZero(purpleMask) / stripSize, PURPLE_NON_ZERO_RATIO_THRESHOLD),
                        LabelMatch("gray", Core.countNonZero(grayMask) / stripSize, GRAY_NON_ZERO_RATIO_THRESHOLD),
                    )
                }
            }
        }
    }
}

private object T1Detector : DeviceRoisAutoSelectorDetector {
    override val result: DeviceRoisAutoSelectorResult = DeviceRoisAutoSelectorResult.T1

    private val pureLabelHsvLower = Scalar(80.0, 60.0, 125.0)
    private val pureLabelHsvUpper = Scalar(110.0, 200.0, 225.0)
    private val farLostLabelGrayBgrLower = Scalar(120.0, 120.0, 120.0)
    private val farLostLabelGrayBgrUpper = Scalar(160.0, 160.0, 160.0)

    private const val PURE_LABEL_NON_ZERO_RATIO_THRESHOLD = 0.08
    private const val FAR_LABEL_NON_ZERO_RATIO_THRESHOLD = 0.06
    private const val LOST_LABEL_NON_ZERO_RATIO_THRESHOLD = 0.06

    private fun maskPureLabel(roiBgr: Mat): Mat = maskBgrInHsv(roiBgr, pureLabelHsvLower, pureLabelHsvUpper)

    private fun maskFarLostLabel(roiBgr: Mat): Mat =
        RgbGrayMasker.gray(
            roiBgr,
            farLostLabelGrayBgrLower,
            farLostLabelGrayBgrUpper,
        )

    override fun analyze(imgBgr: Mat): DetectionReport {
        val rois = DeviceRoisAutoT1(imgBgr.width(), imgBgr.height())

        val labels =
            analyzeLabels(
                imgBgr,
                listOf(
                    Interest("pure", rois.pureLabel, ::maskPureLabel, PURE_LABEL_NON_ZERO_RATIO_THRESHOLD),
                    Interest("far", rois.farLabel, ::maskFarLostLabel, FAR_LABEL_NON_ZERO_RATIO_THRESHOLD),
                    Interest("lost", rois.lostLabel, ::maskFarLostLabel, LOST_LABEL_NON_ZERO_RATIO_THRESHOLD),
                ),
            )

        return DetectionReport(labels)
    }
}

private object T2Detector : DeviceRoisAutoSelectorDetector {
    override val result: DeviceRoisAutoSelectorResult = DeviceRoisAutoSelectorResult.T2

    private val pureLabelHsvLower = Scalar(110.0, 25.0, 90.0)
    private val pureLabelHsvUpper = Scalar(160.0, 150.0, 230.0)
    private val farLabelHsvLower = Scalar(5.0, 25.0, 120.0)
    private val farLabelHsvUpper = Scalar(20.0, 100.0, 240.0)
    private val lostLabelHsvLower = Scalar(160.0, 5.0, 190.0)
    private val lostLabelHsvUpper = Scalar(179.0, 60.0, 255.0)

    private const val PFL_LABEL_NON_ZERO_RATIO_THRESHOLD = 0.3

    private fun hsvInterest(
        name: String,
        rect: DoubleArray,
        hsvLower: Scalar,
        hsvUpper: Scalar,
    ): Interest = Interest(name, rect, { roiBgr -> maskBgrInHsv(roiBgr, hsvLower, hsvUpper) }, PFL_LABEL_NON_ZERO_RATIO_THRESHOLD)

    override fun analyze(imgBgr: Mat): DetectionReport {
        val rois = DeviceRoisAutoT2(imgBgr.width(), imgBgr.height())

        val labels =
            analyzeLabels(
                imgBgr,
                listOf(
                    hsvInterest("pure", rois.pureLabel, pureLabelHsvLower, pureLabelHsvUpper),
                    hsvInterest("far", rois.farLabel, farLabelHsvLower, farLabelHsvUpper),
                    hsvInterest("lost", rois.lostLabel, lostLabelHsvLower, lostLabelHsvUpper),
                ),
            )

        return DetectionReport(labels)
    }
}

private object ArknightsDetector : DeviceRoisAutoSelectorDetector {
    override val result: DeviceRoisAutoSelectorResult = DeviceRoisAutoSelectorResult.T2

    private val pflLabelHsvLower = Scalar(0.0, 5.0, 70.0)
    private val pflLabelHsvUpper = Scalar(179.0, 90.0, 145.0)

    private const val PFL_LABEL_NON_ZERO_RATIO_THRESHOLD = 0.7

    private fun maskPflLabel(roiBgr: Mat): Mat = maskBgrInHsv(roiBgr, pflLabelHsvLower, pflLabelHsvUpper)

    override fun analyze(imgBgr: Mat): DetectionReport {
        val rois = DeviceRoisAutoT2(imgBgr.width(), imgBgr.height())

        val labels =
            analyzeLabels(
                imgBgr,
                listOf(
                    Interest("pure", rois.pureLabel, ::maskPflLabel, PFL_LABEL_NON_ZERO_RATIO_THRESHOLD),
                    Interest("far", rois.farLabel, ::maskPflLabel, PFL_LABEL_NON_ZERO_RATIO_THRESHOLD),
                    Interest("lost", rois.lostLabel, ::maskPflLabel, PFL_LABEL_NON_ZERO_RATIO_THRESHOLD),
                ),
            )

        return DetectionReport(labels)
    }
}

object DeviceRoisAutoSelector {
    private const val LOG_TAG = "DeviceRoisAutoSelector"
    private val logger = Logger.withTag(LOG_TAG)

    private fun formatLabel(label: LabelMatch): String =
        "%s[%s]: %.3f/%.3f".format(label.name, if (label.matched) "Y" else "n", label.ratio, label.threshold)

    private fun logSelectResults(
        strip: StripMatch,
        results: Map<DeviceRoisAutoSelectorDetector, DetectionReport>,
    ) {
        logger.v {
            val resultsSeparated =
                results
                    .map { (detector, report) ->
                        val labels = report.labels.joinToString(", ") { formatLabel(it) }
                        "\t%s (%s, %.3f) - %s".format(
                            detector.javaClass.simpleName,
                            detector.result,
                            report.confidence,
                            labels,
                        )
                    }.joinToString("\n")
                    .ifEmpty { "\t(none)" }
            "Strip - ${formatLabel(strip.purple)}, ${formatLabel(strip.gray)}\nDetector results:\n$resultsSeparated"
        }
    }

    /**
     * Classifies an Arcaea result screenshot into a rois type by its top-left background strip.
     *
     * @param fallback Selected when no skin matches or every detector reports zero confidence.
     */
    fun select(
        imgBgr: Mat,
        fallback: DeviceRoisAutoSelectorResult? = null,
    ): DeviceRoisAutoSelectorResult {
        val strip = StripProbe.analyze(imgBgr)

        val detectors =
            when {
                strip.purple.matched -> listOf(T1Detector, T2Detector)
                strip.gray.matched -> listOf(ArknightsDetector)
                else -> emptyList()
            }
        val results = detectors.associateWith { it.analyze(imgBgr) }
        logSelectResults(strip, results)

        if (results.all { it.value.confidence == 0.0 }) return fallback ?: DeviceRoisAutoSelectorResult.UNKNOWN
        return results.maxBy { it.value.confidence }.key.result
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
