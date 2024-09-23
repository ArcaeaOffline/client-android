package xyz.sevive.arcaeaoffline.ui.common.datetimeeditor

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import xyz.sevive.arcaeaoffline.ui.components.dialogs.DialogConfirmButton
import xyz.sevive.arcaeaoffline.ui.components.dialogs.DialogDismissTextButton
import xyz.sevive.arcaeaoffline.ui.components.preferences.TextPreferencesWidget
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme


@Composable
private fun SecondEditor(
    second: Int,
    onSecondChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selection = remember { TextRange(2, 2) }  // force cursor to the end

    DisableSelection {
        TextField(
            value = TextFieldValue(second.toString(), selection = selection),
            onValueChange = {
                if (it.text.isEmpty()) onSecondChange(0)
                else {
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

@Composable
internal fun DateTimeEditDialog(
    onDismissRequest: () -> Unit,
    dateTime: LocalDateTime,
    minDate: LocalDate? = null,
    onDateTimeChange: (LocalDateTime) -> Unit,
) {
    val dateFormatter = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG) }
    val timeFormatter = remember { DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM) }

    var date by rememberSaveable { mutableStateOf(dateTime.toLocalDate()) }
    var selectedTime by rememberSaveable {
        mutableStateOf(dateTime.toLocalTime().withSecond(0).withNano(0))
    }
    var second by rememberSaveable { mutableIntStateOf(dateTime.toLocalTime().second) }
    val time = remember(selectedTime, second) { selectedTime.withSecond(second) }
    val dateText = remember(date) { dateFormatter.format(date) }
    val timeText = remember(time) { timeFormatter.format(time) }

    var showDateEditDialog by rememberSaveable { mutableStateOf(false) }
    var showTimeEditDialog by rememberSaveable { mutableStateOf(false) }

    if (showDateEditDialog) {
        Dialog(onDismissRequest = { showDateEditDialog = false }) {
            Surface {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    AndroidViewDatePickerDialog(
                        date = date,
                        minDate = minDate,
                        onDateSelect = { date = it },
                    )
                } else {
                    AndroidViewCalendar(
                        date = date,
                        minDate = minDate,
                        onDateSelect = { date = it },
                    )
                }
            }
        }
    }
    if (showTimeEditDialog) {
        Dialog(onDismissRequest = { showTimeEditDialog = false }) {
            Surface {
                AndroidViewTimePicker(time = selectedTime, onTimeSelect = { selectedTime = it })
            }
        }
    }

    AlertDialog(
        onDismissRequest = {},
        confirmButton = {
            DialogConfirmButton(
                onClick = {
                    onDateTimeChange(date.atTime(time))
                    onDismissRequest()
                },
            )
        },
        dismissButton = { DialogDismissTextButton(onClick = onDismissRequest) },
        icon = { Icon(Icons.Default.EditCalendar, contentDescription = null) },
        text = {
            Column {
                TextPreferencesWidget(
                    onClick = { showDateEditDialog = true },
                    leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                    trailingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                    title = dateText,
                )

                Row {
                    TextPreferencesWidget(
                        onClick = { showTimeEditDialog = true },
                        leadingIcon = { Icon(Icons.Default.AccessTime, contentDescription = null) },
                        trailingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        title = timeText,
                        modifier = Modifier.weight(2f),
                    )

                    SecondEditor(
                        second = second,
                        onSecondChange = { second = it },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        },
    )
}

@Preview
@Composable
private fun DateTimeEditDialogRealDevicePreview() {
    var dateTime by remember { mutableStateOf(LocalDateTime.now()) }
    var showDialog by remember { mutableStateOf(true) }

    ArcaeaOfflineTheme {
        Surface {
            if (showDialog) {
                DateTimeEditDialog(
                    onDismissRequest = { showDialog = false },
                    dateTime = dateTime,
                    minDate = LocalDate.of(2017, 6, 16),
                    onDateTimeChange = { dateTime = it },
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))

                Button(onClick = { showDialog = true }) {
                    Text("Open dialog")
                }
            }
        }
    }
}
