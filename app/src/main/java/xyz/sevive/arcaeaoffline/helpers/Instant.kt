package xyz.sevive.arcaeaoffline.helpers

import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle


fun Instant.formatAsLocalizedDateTime(
    formatStyle: FormatStyle = FormatStyle.MEDIUM,
    zone: ZoneId = ZoneId.systemDefault(),
): String = DateTimeFormatter.ofLocalizedDateTime(formatStyle).format(
    LocalDateTime.ofInstant(this, zone)
)

fun Instant.formatAsLocalizedDate(
    formatStyle: FormatStyle = FormatStyle.MEDIUM,
    zone: ZoneId = ZoneId.systemDefault(),
): String = DateTimeFormatter.ofLocalizedDate(formatStyle).format(
    LocalDateTime.ofInstant(this, zone).toLocalDate()
)

fun Instant.formatAsLocalizedTime(
    formatStyle: FormatStyle = FormatStyle.MEDIUM,
    zone: ZoneId = ZoneId.systemDefault(),
): String = DateTimeFormatter.ofLocalizedTime(formatStyle).format(
    LocalDateTime.ofInstant(this, zone).toLocalTime()
)
