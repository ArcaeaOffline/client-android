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
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.number

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
            view.updateDate(date.year, date.month.number - 1, date.day)
            minDate?.let {
                view.minDate = it.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
            }
            maxDate?.let {
                view.maxDate = it.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
            }

            view.setOnDateChangedListener { _, year, monthOfYear, dayOfMonth ->
                onDateSelect(LocalDate(year, monthOfYear + 1, dayOfMonth))
            }
        },
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
            view.date = date.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
            minDate?.let {
                view.minDate = it.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
            }
            maxDate?.let {
                view.maxDate = it.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
            }

            view.setOnDateChangeListener { _, year, month, dayOfMonth ->
                onDateSelect(LocalDate(year, month + 1, dayOfMonth))
            }
        },
    )
}

@Composable
internal fun AndroidViewTimePicker(
    time: LocalTime,
    onTimeSelect: (LocalTime) -> Unit,
) {
    AndroidView(
        modifier = Modifier.wrapContentSize(),
        factory = { context -> TimePicker(context) },
        update = { view ->
            view.hour = time.hour
            view.minute = time.minute

            view.setOnTimeChangedListener { _, hourOfDay, minute ->
                onTimeSelect(LocalTime(hourOfDay, minute))
            }
        },
    )
}
