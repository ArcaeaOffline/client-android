package xyz.sevive.arcaeaoffline.helpers

import ai.onnxruntime.OnnxModelMetadata
import org.threeten.bp.Instant
import xyz.sevive.arcaeaoffline.core.ocr.device.DeviceOcrOnnxHelper

enum class OcrDependencyStatus { OK, ERROR, WARNING, ABSENCE, UNKNOWN }

interface OcrDependencyStatusDetail {
    val absence: Boolean
    val exception: Exception?

    fun status(): OcrDependencyStatus
    fun summary(): String?
    fun details(): String? {
        if (exception == null) return null
        return exception!!.message ?: exception.toString()
    }
}

data class KNearestModelStatusDetail(
    override val absence: Boolean = false,
    override val exception: Exception? = null,
    val varCount: Int? = null,
    val isTrained: Boolean = false,
) : OcrDependencyStatusDetail {
    override fun status(): OcrDependencyStatus {
        if (absence) return OcrDependencyStatus.ABSENCE
        if (exception != null || !isTrained) return OcrDependencyStatus.ERROR

        return when (varCount) {
            null -> OcrDependencyStatus.UNKNOWN
            81 -> OcrDependencyStatus.OK

            else -> OcrDependencyStatus.WARNING
        }
    }

    override fun summary(): String? {
        return when {
            exception != null -> exception::class.simpleName ?: "Error"
            varCount != null -> "varCount $varCount"
            else -> null
        }
    }
}

data class ImageHashesDatabaseStatusDetail(
    override val absence: Boolean = false,
    override val exception: Exception? = null,
    val jacketCount: Int? = null,
    val partnerIconCount: Int? = null,
    val builtTime: Instant? = null,
) : OcrDependencyStatusDetail {
    override fun status(): OcrDependencyStatus {
        if (absence) return OcrDependencyStatus.ABSENCE
        if (exception != null) return OcrDependencyStatus.ERROR

        return when {
            jacketCount == null || partnerIconCount == null -> OcrDependencyStatus.ERROR
            jacketCount == 0 || partnerIconCount == 0 -> OcrDependencyStatus.WARNING
            else -> OcrDependencyStatus.OK
        }
    }

    override fun summary(): String? {
        if (absence) return null
        if (exception != null) return exception::class.simpleName ?: "Error"

        val parts = mutableListOf<String>()
        jacketCount?.let { parts.add("J$it") } ?: parts.add("Jx")
        partnerIconCount?.let { parts.add("PI$it") } ?: parts.add("PIx")
        builtTime?.let { parts.add(builtTime.formatAsLocalizedDateTime()) }

        return parts.joinToString(", ")
    }
}

data class CrnnModelStatusDetail(
    override val absence: Boolean = false,
    override val exception: Exception? = null,
    val modelMetadata: OnnxModelMetadata? = null,
    val inputNames: Set<String>? = null,
    val outputNames: Set<String>? = null,
) : OcrDependencyStatusDetail {
    override fun status(): OcrDependencyStatus {
        if (absence) return OcrDependencyStatus.ABSENCE
        if (exception != null) return OcrDependencyStatus.ERROR

        return when {
            modelMetadata == null -> OcrDependencyStatus.UNKNOWN
            modelMetadata.version == 0L -> OcrDependencyStatus.WARNING
            else -> OcrDependencyStatus.OK
        }
    }

    private val builtTimestampRaw get() = modelMetadata?.customMetadata?.get("built_timestamp")
    private val builtTimestamp = builtTimestampRaw?.let { Instant.ofEpochSecond(it.toLong()) }
    private val builtTimestampReadable = builtTimestamp?.formatAsLocalizedDateTime()

    override fun summary(): String? {
        if (absence) return null
        if (exception != null) return exception::class.simpleName ?: "Error"
        if (modelMetadata == null) return null

        val parts = mutableListOf<String>()

        parts.add(DeviceOcrOnnxHelper.modelVersionString(modelMetadata.version))
        builtTimestampReadable?.let { parts.add(it) }

        return parts.filter { it.isNotEmpty() }.joinToString(", ")
    }

    override fun details(): String? {
        val original = super.details()
        if (original != null) return original

        val parts = mutableListOf<String>()

        modelMetadata?.let {
            parts.add("version: ${it.version} (${DeviceOcrOnnxHelper.modelVersion(it.version)})")
            parts.add("producer: ${it.producerName}")
            parts.add("domain: ${it.domain}")
            parts.add("graph_name: ${it.graphName}")
        }

        parts.add("inputs: $inputNames")
        parts.add("outputs: $outputNames")

        builtTimestampRaw?.let { parts.add("built_timestamp: $it ($builtTimestamp)") }

        return when (val res = parts.joinToString("\n")) {
            "" -> null
            else -> res
        }
    }
}
