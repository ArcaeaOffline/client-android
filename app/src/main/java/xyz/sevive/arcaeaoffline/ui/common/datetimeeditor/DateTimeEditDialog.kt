package xyz.sevive.arcaeaoffline.ui.common.datetimeeditor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.atTime
import kotlinx.datetime.format
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalTime
import kotlinx.datetime.toLocalDateTime
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.components.dialogs.DialogConfirmButton
import xyz.sevive.arcaeaoffline.ui.components.dialogs.DialogDismissTextButton
import xyz.sevive.arcaeaoffline.ui.components.preferences.TextPreferencesWidget
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.time.Clock
import kotlin.time.Instant

@Composable
private fun SecondEditor(
    second: Int,
    onSecondChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selection = remember { TextRange(2, 2) } // force cursor to the end

    DisableSelection {
        TextField(
            value = TextFieldValue(second.toString(), selection = selection),
            onValueChange = {
                if (it.text.isEmpty()) {
                    onSecondChange(0)
                } else {
                    it.text.toIntOrNull()?.let { int -> if (int in 0..59) onSecondChange(int) }
                }
            },
            modifier = modifier,
            singleLine = true,
            leadingIcon = {
                IconButton(onClick = { if (second > 0) onSecondChange(second - 1) }) {
                    Icon(Icons.Default.Remove, contentDescription = null)
                }
            },
            trailingIcon = {
                IconButton(onClick = { if (second < 59) onSecondChange(second + 1) }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DateTimeEditDialog(
    onDismissRequest: () -> Unit,
    dateTime: LocalDateTime,
    minDate: LocalDate? = null,
    onDateTimeChange: (LocalDateTime) -> Unit,
) {
    val dateFormatter = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG) }
    val timeFormatter = remember { DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM) }

    val minDateMillis = remember(minDate) { minDate?.atStartOfDayIn(TimeZone.UTC)?.toEpochMilliseconds() }
    val datePickerState =
        rememberDatePickerState(
            initialSelectedDate = dateTime.date.toJavaLocalDate(),
            selectableDates =
                object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean = minDateMillis?.let { utcTimeMillis >= it } ?: true
                },
        )
    val timePickerState =
        rememberTimePickerState(
            initialHour = dateTime.time.hour,
            initialMinute = dateTime.time.minute,
        )

    var date by rememberSaveable { mutableStateOf(dateTime.date) }
    var hour by rememberSaveable { mutableIntStateOf(dateTime.time.hour) }
    var minute by rememberSaveable { mutableIntStateOf(dateTime.time.minute) }
    var second by rememberSaveable { mutableIntStateOf(dateTime.time.second) }
    val selectedDateTime by remember {
        derivedStateOf { date.atTime(LocalTime(hour, minute, second)) }
    }
    val dateText by remember {
        derivedStateOf { dateFormatter.format(selectedDateTime.date.toJavaLocalDate()) }
    }
    val timeText by remember {
        derivedStateOf { timeFormatter.format(selectedDateTime.time.toJavaLocalTime()) }
    }

    var showDateEditDialog by rememberSaveable { mutableStateOf(false) }
    var showTimeEditDialog by rememberSaveable { mutableStateOf(false) }

    if (showDateEditDialog) {
        val onDatePickerDismiss = { showDateEditDialog = false }
        DatePickerDialog(
            onDismissRequest = onDatePickerDismiss,
            confirmButton = {
                DialogConfirmButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        date = Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.UTC).date
                    }
                    onDatePickerDismiss()
                })
            },
            dismissButton = {
                DialogDismissTextButton(onClick = onDatePickerDismiss)
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
    if (showTimeEditDialog) {
        val onTimePickerDismiss = { showTimeEditDialog = false }
        TimePickerDialog(
            onDismissRequest = onTimePickerDismiss,
            title = {},
            confirmButton = {
                DialogConfirmButton(onClick = {
                    hour = timePickerState.hour
                    minute = timePickerState.minute
                    onTimePickerDismiss()
                })
            },
            dismissButton = {
                DialogDismissTextButton(onClick = onTimePickerDismiss)
            },
        ) {
            TimePicker(
                state = timePickerState,
            )
        }
    }

    AlertDialog(
        onDismissRequest = {},
        confirmButton = {
            DialogConfirmButton(
                onClick = {
                    onDateTimeChange(selectedDateTime)
                    onDismissRequest()
                },
            )
        },
        dismissButton = { DialogDismissTextButton(onClick = onDismissRequest) },
        icon = { Icon(Icons.Default.EditCalendar, contentDescription = null) },
        text = {
            LazyColumn {
                item {
                    TextPreferencesWidget(
                        onClick = { showDateEditDialog = true },
                        leadingIcon = Icons.Default.CalendarMonth,
                        leadingIconTint = MaterialTheme.colorScheme.secondary,
                        trailingIcon = Icons.Default.Edit,
                        title = dateText,
                        content = stringResource(R.string.datetime_picker_date),
                    )
                }

                item {
                    TextPreferencesWidget(
                        onClick = { showTimeEditDialog = true },
                        leadingIcon = Icons.Default.AccessTime,
                        leadingIconTint = MaterialTheme.colorScheme.secondary,
                        trailingIcon = Icons.Default.Edit,
                        title = timeText,
                        content = stringResource(R.string.datetime_picker_time),
                    )
                }

                item {
                    SecondEditor(
                        second = second,
                        onSecondChange = { second = it },
                    )
                }
            }
        },
    )
}

@Preview
@Composable
private fun DateTimeEditDialogRealDevicePreview() {
    var dateTime by remember { mutableStateOf(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())) }
    var showDialog by remember { mutableStateOf(true) }

    ArcaeaOfflineTheme {
        Surface {
            if (showDialog) {
                DateTimeEditDialog(
                    onDismissRequest = { showDialog = false },
                    dateTime = dateTime,
                    minDate = LocalDate(2017, 6, 16),
                    onDateTimeChange = { dateTime = it },
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = dateTime.format(LocalDateTime.Formats.ISO))

                Button(onClick = { showDialog = true }) {
                    Text("Open dialog")
                }
            }
        }
    }
}
