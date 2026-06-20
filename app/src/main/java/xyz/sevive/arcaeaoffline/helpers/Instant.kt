package xyz.sevive.arcaeaoffline.helpers

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toJavaLocalTime
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.time.Instant as KotlinInstant

fun KotlinInstant.formatAsLocalizedDateTime(): String =
    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).format(
        this.toLocalDateTime(TimeZone.currentSystemDefault()).toJavaLocalDateTime(),
    )

fun KotlinInstant.formatAsLocalizedDate(): String =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).format(
        this.toLocalDateTime(TimeZone.currentSystemDefault()).date.toJavaLocalDate(),
    )

fun KotlinInstant.formatAsLocalizedTime(): String =
    DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM).format(
        this.toLocalDateTime(TimeZone.currentSystemDefault()).time.toJavaLocalTime(),
    )
