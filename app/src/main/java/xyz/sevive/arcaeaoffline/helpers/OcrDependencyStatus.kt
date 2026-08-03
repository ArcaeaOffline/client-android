package xyz.sevive.arcaeaoffline.helpers

import kotlin.time.Instant

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
    val modelVersion: List<Int>? = null,
    val producerName: String? = null,
    val producerVersion: String? = null,
    val domain: String? = null,
    val graphName: String? = null,
    val inputNames: Set<String>? = null,
    val outputNames: Set<String>? = null,
    val builtTimestamp: Instant? = null,
    val patchedTimestamp: Instant? = null,
) : OcrDependencyStatusDetail {
    override fun status(): OcrDependencyStatus {
        if (absence) return OcrDependencyStatus.ABSENCE
        if (exception != null) return OcrDependencyStatus.ERROR

        return when {
            modelVersion.isNullOrEmpty() -> OcrDependencyStatus.UNKNOWN
            else -> OcrDependencyStatus.OK
        }
    }

    private val modelVersionString = modelVersion?.takeIf { it.isNotEmpty() }?.let { "v" + it.joinToString(".") }

    override fun summary(): String? {
        if (absence) return null
        if (exception != null) return exception::class.simpleName ?: "Error"

        val parts = mutableListOf<String>()

        modelVersionString?.let { parts.add(it) }
        builtTimestamp?.let { parts.add(it.formatAsLocalizedDateTime()) }

        return parts.filter { it.isNotEmpty() }.joinToString(", ")
    }

    override fun details(): String? {
        val original = super.details()
        if (original != null) return original

        val parts = mutableListOf<String>()

        modelVersion?.takeIf { it.isNotEmpty() }?.let {
            parts.add("version: $it ($modelVersionString)")
        }
        producerName?.let { parts.add("producer: $it") }
        producerVersion?.let { parts.add("producer_version: $it") }
        domain?.let { parts.add("domain: $it") }
        graphName?.let { parts.add("graph_name: $it") }

        parts.add("inputs: $inputNames")
        parts.add("outputs: $outputNames")

        builtTimestamp?.let { parts.add("built: $it") }
        patchedTimestamp?.let { parts.add("patched: $it") }

        return when (val res = parts.joinToString("\n")) {
            "" -> null
            else -> res
        }
    }
}
