package xyz.sevive.arcaeaoffline.ui.common.datetimeeditor

import android.os.Build
import android.widget.CalendarView
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneOffset


@RequiresApi(Build.VERSION_CODES.O)
@Composable
internal fun AndroidViewDatePickerDialog(
    date: LocalDate,
    minDate: LocalDate? = null,
    maxDate: LocalDate? = null,
    onDateSelect: (LocalDate) -> Unit,
) {
    AndroidView(
        modifier = Modifier.wrapContentSize(),
        factory = { context -> DatePicker(context) },
        update = { view ->
            view.updateDate(date.year, date.monthValue - 1, date.dayOfMonth)
            minDate?.let {
                view.minDate = it.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
            }
            maxDate?.let {
                view.maxDate = it.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
            }

            view.setOnDateChangedListener { _, year, monthOfYear, dayOfMonth ->
                onDateSelect(
                    LocalDate
                        .now()
                        .withMonth(monthOfYear + 1)
                        .withYear(year)
                        .withDayOfMonth(dayOfMonth)
                )
            }
        }
    )
}

@Composable
internal fun AndroidViewCalendar(
    date: LocalDate,
    minDate: LocalDate? = null,
    maxDate: LocalDate? = null,
    onDateSelect: (LocalDate) -> Unit,
) {
    AndroidView(
        modifier = Modifier.wrapContentSize(),
        factory = { context -> CalendarView(context) },
        update = { view ->
            view.date = date.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
            minDate?.let {
                view.minDate = it.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
            }
            maxDate?.let {
                view.maxDate = it.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
            }

            view.setOnDateChangeListener { _, year, month, dayOfMonth ->
                onDateSelect(
                    LocalDate
                        .now()
                        .withMonth(month + 1)
                        .withYear(year)
                        .withDayOfMonth(dayOfMonth)
                )
            }
        }
    )
}

@Composable
internal fun AndroidViewTimePicker(time: LocalTime, onTimeSelect: (LocalTime) -> Unit) {
    AndroidView(
        modifier = Modifier.wrapContentSize(),
        factory = { context -> TimePicker(context) },
        update = { view ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                view.hour = time.hour
                view.minute = time.minute
            } else {
                @Suppress("DEPRECATION")
                view.currentHour = time.hour
                @Suppress("DEPRECATION")
                view.currentMinute = time.minute
            }

            view.setOnTimeChangedListener { _, hourOfDay, minute ->
                onTimeSelect(LocalTime.of(hourOfDay, minute))
            }
        }
    )
}
