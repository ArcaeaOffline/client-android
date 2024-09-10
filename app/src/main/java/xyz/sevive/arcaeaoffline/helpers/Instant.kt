package xyz.sevive.arcaeaoffline.helpers

import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle


fun Instant.formatAsLocalizedDateTime(
    dateTimeStyle: FormatStyle = FormatStyle.MEDIUM,
    zone: ZoneId = ZoneId.systemDefault(),
): String {
    return DateTimeFormatter.ofLocalizedDateTime(dateTimeStyle).format(
        LocalDateTime.ofInstant(this, zone)
    )
}
