package xyz.sevive.arcaeaoffline.core.ocr.device

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * JVM unit tests for [collectMetadataMismatches], using the field values of
 * the real bundled model assets as the matching baseline.
 */
class ModelMetadataVerificationTest {
    private val training =
        DeviceOcrOnnxHelper.ModelInfo(
            imageHeight = 50,
            imageWidth = 220,
            labels = listOf("∅", "-", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"),
            blankToken = "∅",
            padToken = "-",
            builtTimestamp = 1728488367L,
        )

    private val patch =
        DeviceOcrOnnxHelper.ModelPatchInfo(
            version = listOf(1, 0, 3),
            producerName = "pytorch",
            producerVersion = "2.4.1",
            domain = "",
            graphName = "crnn_patched",
            inputNames = listOf("raw_image"),
            outputNames = listOf("model_output", "decoded_output"),
            patchedTimestamp = 1785689264L,
        )

    // encode_semver(1, 0, 3)
    private val encodedVersion = (1L shl 48) or (0L shl 32) or 3L

    private fun customMetadata(
        builtTimestamp: String? = "1728488367",
        patchedTimestamp: String? = "1785689264",
    ): Map<String, String> =
        buildMap {
            put("image_width", "220")
            put("image_height", "50")
            put("blank_token", "∅")
            put("pad_token", "-")
            builtTimestamp?.let { put("built_timestamp", it) }
            patchedTimestamp?.let { put("patched_timestamp", it) }
        }

    private fun mismatches(
        patchBlock: DeviceOcrOnnxHelper.ModelPatchInfo? = patch,
        trainingBlock: DeviceOcrOnnxHelper.ModelInfo = training,
        metadata: Map<String, String> = customMetadata(),
        version: Long = encodedVersion,
        producerName: String? = "pytorch",
        domain: String? = "",
        graphName: String? = "crnn_patched",
        inputNames: Set<String> = setOf("raw_image"),
        outputNames: Set<String> = setOf("model_output", "decoded_output"),
    ): List<String> =
        collectMetadataMismatches(
            modelVersion = version,
            producerName = producerName,
            domain = domain,
            graphName = graphName,
            customMetadata = metadata,
            inputNames = inputNames,
            outputNames = outputNames,
            infoFile = DeviceOcrOnnxHelper.ModelInfoFile(training = trainingBlock, patch = patchBlock),
        )

    @Test
    fun matchingAssetProducesNoMismatches() {
        assertEquals(emptyList<String>(), mismatches())
    }

    @Test
    fun missingPatchBlockIsReported() {
        assertEquals(
            listOf("model_info.json is missing the `patch` block"),
            mismatches(patchBlock = null),
        )
    }

    @Test
    fun versionMismatchIsReported() {
        val result = mismatches(version = (1L shl 48) or 4L)
        assertTrue(result.any { it.startsWith("version:") })
    }

    @Test
    fun producerNameMismatchIsReported() {
        val result = mismatches(producerName = "other-producer")
        assertTrue(result.any { it.startsWith("producer_name:") })
    }

    @Test
    fun inputNamesMismatchIsReported() {
        val result = mismatches(inputNames = setOf("image"))
        assertTrue(result.any { it.startsWith("input_names:") })
    }

    @Test
    fun missingCustomMetadataKeyIsReported() {
        val result = mismatches(metadata = customMetadata(builtTimestamp = null))
        assertTrue(result.any { it.startsWith("`built_timestamp`:") })
    }

    @Test
    fun nonNumericTimestampIsReported() {
        val result = mismatches(metadata = customMetadata(patchedTimestamp = "not-a-number"))
        assertTrue(result.any { it.startsWith("`patched_timestamp`:") })
    }

    @Test
    fun bothSidesMissingTimestampIsNotReported() {
        // json built_timestamp defaults to 0 (not written), matching an
        // absent model metadata key
        val result = mismatches(trainingBlock = training.copy(builtTimestamp = 0L), metadata = customMetadata(builtTimestamp = null))
        assertTrue(result.isEmpty())
    }
}
