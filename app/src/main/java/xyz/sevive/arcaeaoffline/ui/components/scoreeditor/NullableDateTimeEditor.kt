package xyz.sevive.arcaeaoffline.ui.components.scoreeditor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DateTimeEditDialog(
    date: LocalDateTime?,
    onConfirm: (LocalDateTime) -> Unit,
    onDismiss: () -> Unit,
) {
    val dateFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
    val zoneId = ZoneId.systemDefault()

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = (date?.toEpochSecond(ZoneOffset.UTC) ?: LocalDateTime.now()
            .toEpochSecond(ZoneOffset.UTC)) * 1000,
        yearRange = IntRange(2017, 2100),
    )
    val timePickerState = rememberTimePickerState(
        initialHour = date?.hour ?: 0,
        initialMinute = date?.minute ?: 0,
    )

    val pickerStateToLocalDateTime: () -> LocalDateTime = {
        val instant = Instant.ofEpochMilli(datePickerState.selectedDateMillis ?: 0)
        val localDate = instant.atZone(zoneId).toLocalDate()

        LocalDateTime.of(
            localDate, LocalTime.of(timePickerState.hour, timePickerState.minute)
        )
    }

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf(R.string.datetime_picker_date_tab, R.string.datetime_picker_time_tab)
    val tabIcons = listOf(Icons.Default.CalendarMonth, Icons.Default.Schedule)

    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Scaffold(
            topBar = {
                TabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, titleId ->
                        Tab(
                            text = { Text(stringResource(titleId)) },
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            icon = { Icon(tabIcons[index], null) },
                        )
                    }
                }
            },
            bottomBar = {
                Row(
                    Modifier.padding(dimensionResource(R.dimen.general_page_padding)),
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_arrangement_padding)),
                ) {
                    Spacer(Modifier.weight(1f))

                    Button(onClick = { onDismiss() }) {
                        Text(stringResource(R.string.general_cancel))
                    }
                    Button(onClick = { onConfirm(pickerStateToLocalDateTime()) }) {
                        Text(stringResource(R.string.general_ok))
                    }
                }
            },
        ) { padding ->
            Scaffold(
                bottomBar = {
                    Text(pickerStateToLocalDateTime().format(dateFormatter))
                },
                modifier = Modifier
                    .padding(padding)
                    .padding(dimensionResource(R.dimen.general_page_padding)),
            ) {
                Surface(Modifier.padding(it)) {
                    Column(
                        Modifier
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Spacer(Modifier.weight(1f))

                        when (selectedTab) {
                            0 -> DatePicker(datePickerState, Modifier.fillMaxWidth())
                            1 -> TimePicker(timePickerState, Modifier.fillMaxWidth())
                        }

                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
internal fun NullableDateTimeEditor(
    date: LocalDateTime?,
    onDateChange: (LocalDateTime) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showPicker by rememberSaveable { mutableStateOf(false) }
    val editable = date != null

    DisableSelection {
        TextField(
            value = if (date != null) date.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)) else "",
            onValueChange = {},
            modifier = modifier.clickable(editable) { showPicker = true },
            readOnly = true,
            enabled = false,
            label = { Text(stringResource(R.string.datetime_picker_label)) },
            colors = if (date != null) TextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
//                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
            ) else TextFieldDefaults.colors(),
            trailingIcon = {
                IconButton(onClick = { showPicker = true }, enabled = editable) {
                    Icon(Icons.Default.Edit, null)
                }
            },
        )
    }

    if (showPicker) {
        DateTimeEditDialog(
            date = date,
            onConfirm = {
                onDateChange(it)
                showPicker = false
            },
            onDismiss = { showPicker = false },
        )
    }
}

@Preview
@Composable
private fun NullableDateTimeEditorPreview() {
    var date by remember { mutableStateOf(LocalDateTime.now()) }

    ArcaeaOfflineTheme {
        NullableDateTimeEditor(date = date, onDateChange = { date = it })
    }
}
