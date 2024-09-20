package xyz.sevive.arcaeaoffline.ui.common.datetimeeditor

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
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
import xyz.sevive.arcaeaoffline.helpers.activity.calculateWindowSizeClass
import xyz.sevive.arcaeaoffline.helpers.context.findActivity
import xyz.sevive.arcaeaoffline.ui.common.customtab.CustomTab
import xyz.sevive.arcaeaoffline.ui.common.customtab.CustomTabIndicatorPosition
import xyz.sevive.arcaeaoffline.ui.components.IconRow

/**
 * FUCK COMPOSE
 */

@OptIn(ExperimentalMaterial3Api::class)
internal fun dateTimePickerStateToLocalDateTime(
    datePickerState: DatePickerState,
    timePickerState: TimePickerState,
    zoneId: ZoneId = ZoneId.systemDefault(),
): LocalDateTime {
    val instant = Instant.ofEpochMilli(datePickerState.selectedDateMillis ?: 0)
    val localDate = instant.atZone(zoneId).toLocalDate()

    return LocalDateTime.of(
        localDate, LocalTime.of(timePickerState.hour, timePickerState.minute)
    )
}

private data class DateTimeEditDialogTab(
    @StringRes val textId: Int, val icon: ImageVector
)

private val DateTimeEditDialogTabs = listOf(
    DateTimeEditDialogTab(R.string.datetime_picker_date_tab, Icons.Default.CalendarMonth),
    DateTimeEditDialogTab(R.string.datetime_picker_time_tab, Icons.Default.Schedule),
)

@Composable
internal fun DateTimeEditDialogBottomBar(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    HorizontalDivider()

    Row(
        Modifier.padding(dimensionResource(R.dimen.page_padding)),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_padding)),
    ) {
        Spacer(Modifier.weight(1f))

        TextButton(
            onClick = onDismiss,
            colors = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.secondary
            ),
        ) {
            IconRow {
                Icon(Icons.Default.Close, null)
                Text(stringResource(R.string.general_cancel))
            }
        }

        Button(onClick = onConfirm) {
            IconRow {
                Icon(Icons.Default.Check, null)
                Text(stringResource(R.string.general_ok))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DateTimeEditDialogContent(
    datePickerState: DatePickerState,
    timePickerState: TimePickerState,
    selectedTab: Int,
    onSelectedTabChange: (Int) -> Unit,
    formattedDate: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    Scaffold(
        Modifier.fillMaxHeight(),
        topBar = {
            Column {
                TabRow(selectedTabIndex = selectedTab) {
                    DateTimeEditDialogTabs.forEachIndexed { index, tabItem ->
                        Tab(
                            text = { Text(stringResource(tabItem.textId)) },
                            selected = selectedTab == index,
                            onClick = { onSelectedTabChange(index) },
                            icon = { Icon(tabItem.icon, null) },
                        )
                    }
                }
            }
        },
        bottomBar = {
            DateTimeEditDialogBottomBar(onDismiss = onDismiss, onConfirm = onConfirm)
        },
    ) { padding ->
        Scaffold(
            Modifier
                .padding(padding)
                .padding(dimensionResource(R.dimen.page_padding)),
            bottomBar = { Text(formattedDate) },
        ) {
            Column(
                Modifier
                    .padding(it)
                    .verticalScroll(rememberScrollState())
            ) {
                when (selectedTab) {
                    0 -> DatePicker(datePickerState, Modifier.fillMaxWidth())
                    1 -> TimePicker(timePickerState, Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimeEditDialogContentExpanded(
    datePickerState: DatePickerState,
    timePickerState: TimePickerState,
    selectedTab: Int,
    onSelectedTabChange: (Int) -> Unit,
    formattedDate: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    Scaffold(bottomBar = {
        DateTimeEditDialogBottomBar(onDismiss = onDismiss, onConfirm = onConfirm)
    }) { padding ->
        Scaffold(
            Modifier.padding(padding),
            bottomBar = { Text(formattedDate) },
        ) {
            Row(Modifier.padding(it)) {
                Column {
                    DateTimeEditDialogTabs.forEachIndexed { index, tabItem ->
                        CustomTab(
                            selected = selectedTab == index,
                            onClick = { onSelectedTabChange(index) },
                            text = stringResource(tabItem.textId),
                            icon = { Icon(tabItem.icon, null) },
                            indicatorPosition = CustomTabIndicatorPosition.END,
                        )
                    }
                }

                Column(
                    Modifier
                        .padding(dimensionResource(R.dimen.page_padding))
                        .verticalScroll(rememberScrollState())
                ) {
                    when (selectedTab) {
                        0 -> DatePicker(datePickerState, Modifier.fillMaxWidth())
                        1 -> TimePicker(timePickerState, Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DateTimeEditDialog(
    date: LocalDateTime?,
    onDismiss: () -> Unit,
    onConfirm: (LocalDateTime) -> Unit,
) {
    val dateFormatter = remember { DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = (date?.toEpochSecond(ZoneOffset.UTC) ?: LocalDateTime.now()
            .toEpochSecond(ZoneOffset.UTC)) * 1000,
        yearRange = IntRange(2017, 2100),
    )
    val timePickerState = rememberTimePickerState(
        initialHour = date?.hour ?: 0,
        initialMinute = date?.minute ?: 0,
    )

    val pickerStateToLocalDateTime = remember {
        fun(): LocalDateTime {
            return dateTimePickerStateToLocalDateTime(datePickerState, timePickerState)
        }
    }

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    val widthSizeClass =
        LocalContext.current.findActivity()?.calculateWindowSizeClass()?.widthSizeClass
    val expanded = remember(widthSizeClass) {
        widthSizeClass != null && widthSizeClass >= WindowWidthSizeClass.Expanded
    }

    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        if (expanded) {
            DateTimeEditDialogContentExpanded(
                datePickerState = datePickerState,
                timePickerState = timePickerState,
                selectedTab = selectedTab,
                onSelectedTabChange = { selectedTab = it },
                formattedDate = pickerStateToLocalDateTime().format(dateFormatter),
                onDismiss = onDismiss,
                onConfirm = { onConfirm(pickerStateToLocalDateTime()) },
            )
        } else {
            DateTimeEditDialogContent(
                datePickerState = datePickerState,
                timePickerState = timePickerState,
                selectedTab = selectedTab,
                onSelectedTabChange = { selectedTab = it },
                formattedDate = pickerStateToLocalDateTime().format(dateFormatter),
                onDismiss = onDismiss,
                onConfirm = { onConfirm(pickerStateToLocalDateTime()) },
            )
        }
    }
}
