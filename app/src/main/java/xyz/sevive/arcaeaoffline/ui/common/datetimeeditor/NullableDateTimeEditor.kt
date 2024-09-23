package xyz.sevive.arcaeaoffline.ui.common.datetimeeditor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.jakewharton.threetenabp.AndroidThreeTen
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme


@Composable
internal fun NullableDateTimeEditor(
    dateTime: LocalDateTime?,
    onDateTimeChange: (LocalDateTime) -> Unit,
    modifier: Modifier = Modifier,
    minDate: LocalDate? = null,
) {
    val dateTimeFormatter = remember { DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM) }
    var showEditor by rememberSaveable { mutableStateOf(false) }
    val editable = dateTime != null

    DisableSelection {
        TextField(
            value = dateTime?.format(dateTimeFormatter) ?: "",
            onValueChange = {},
            modifier = modifier.clickable(editable) { showEditor = true },
            readOnly = true,
            enabled = false,
            label = { Text(stringResource(R.string.datetime_picker_label)) },
            colors = if (dateTime != null) TextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
//                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
            ) else TextFieldDefaults.colors(),
            trailingIcon = {
                IconButton(onClick = { showEditor = true }, enabled = editable) {
                    Icon(Icons.Default.Edit, null)
                }
            },
        )
    }

    if (showEditor && dateTime != null) {
        DateTimeEditDialog(
            dateTime = dateTime,
            minDate = minDate,
            onDateTimeChange = {
                onDateTimeChange(it)
                showEditor = false
            },
            onDismissRequest = { showEditor = false },
        )
    }
}

@Preview
@Composable
private fun NullableDateTimeEditorPreview() {
    AndroidThreeTen.init(LocalContext.current)
    var dateTime by remember { mutableStateOf(LocalDateTime.now()) }

    ArcaeaOfflineTheme {
        NullableDateTimeEditor(dateTime = dateTime, onDateTimeChange = { dateTime = it })
    }
}
