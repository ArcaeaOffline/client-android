package xyz.sevive.arcaeaoffline.core.ocr

import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle

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
        if (exception != null) exception::class.simpleName ?: "Error"

        val parts = mutableListOf<String>()
        jacketCount?.let { parts.add("J$it") } ?: parts.add("Jx")
        partnerIconCount?.let { parts.add("PI$it") } ?: parts.add("PIx")
        builtTime?.let {
            parts.add(
                DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                    .format(LocalDateTime.ofInstant(builtTime, ZoneId.systemDefault()))
            )
        }

        return parts.joinToString(", ")
    }
}
