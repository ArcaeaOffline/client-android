package xyz.sevive.arcaeaoffline.helpers

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

fun Instant.formatAsLocalizedDateTime(
    formatStyle: FormatStyle = FormatStyle.MEDIUM,
    zone: ZoneId = ZoneId.systemDefault(),
): String =
    DateTimeFormatter.ofLocalizedDateTime(formatStyle).format(
        LocalDateTime.ofInstant(this, zone),
    )

fun Instant.formatAsLocalizedDate(
    formatStyle: FormatStyle = FormatStyle.MEDIUM,
    zone: ZoneId = ZoneId.systemDefault(),
): String =
    DateTimeFormatter.ofLocalizedDate(formatStyle).format(
        LocalDateTime.ofInstant(this, zone).toLocalDate(),
    )

fun Instant.formatAsLocalizedTime(
    formatStyle: FormatStyle = FormatStyle.MEDIUM,
    zone: ZoneId = ZoneId.systemDefault(),
): String =
    DateTimeFormatter.ofLocalizedTime(formatStyle).format(
        LocalDateTime.ofInstant(this, zone).toLocalTime(),
    )
