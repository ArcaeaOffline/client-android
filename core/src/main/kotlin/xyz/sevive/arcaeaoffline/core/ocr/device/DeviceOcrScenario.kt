package xyz.sevive.arcaeaoffline.core.ocr.device

import ai.onnxruntime.OrtSession
import org.opencv.core.Mat
import xyz.sevive.arcaeaoffline.core.ocr.ImageHashesDatabase
import xyz.sevive.arcaeaoffline.core.ocr.OcrScenario
import xyz.sevive.arcaeaoffline.core.ocr.device.rois.DeviceRoisAutoSelector
import xyz.sevive.arcaeaoffline.core.ocr.device.rois.DeviceRoisAutoSelectorResult
import xyz.sevive.arcaeaoffline.core.ocr.device.rois.extractor.DeviceRoisExtractor
import xyz.sevive.arcaeaoffline.core.ocr.device.rois.masker.DeviceRoisMasker

/**
 * A device screenshot OCR scenario: owns the black-edge-cropped image and the extractor
 * and masker built from it. The [extractor]'s ROI properties are submat views of the
 * cropped image and are invalid after [close].
 */
class DeviceOcrScenario private constructor(
    private val imgCropped: Mat,
    val extractor: DeviceRoisExtractor,
    private val masker: DeviceRoisMasker,
) : OcrScenario,
    AutoCloseable {
    companion object {
        /**
         * Builds a scenario from a raw BGR screenshot. The rois type (T1/T2) is auto-detected
         * on the black-edge-cropped image.
         *
         * @param fallback Selected when every detector reports zero confidence.
         * @throws IllegalArgumentException when the rois type cannot be determined and
         *   [fallback] is null.
         */
        fun forImage(
            imgBgr: Mat,
            fallback: DeviceRoisAutoSelectorResult? = null,
        ): DeviceOcrScenario {
            val imgCropped = CropBlackEdges.cropOrOriginal(imgBgr)
            val roisType = DeviceRoisAutoSelector.select(imgCropped, fallback)
            require(roisType != DeviceRoisAutoSelectorResult.UNKNOWN) {
                "Cannot determine device rois type; pass an explicit fallback"
            }

            val (rois, masker) =
                DeviceRoisAutoSelector.createRoisAndMasker(
                    roisType,
                    imgCropped.width(),
                    imgCropped.height(),
                )
            return DeviceOcrScenario(imgCropped, DeviceRoisExtractor(rois, imgCropped), masker)
        }
    }

    fun ocr(
        ortSession: OrtSession,
        hashesDb: ImageHashesDatabase,
    ): DeviceOcrResult = DeviceOcr(extractor, masker, ortSession, hashesDb).ocr()

    override fun close() {
        imgCropped.release()
    }
}
