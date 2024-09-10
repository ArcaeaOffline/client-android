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
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme


@Composable
internal fun NullableDateTimeEditor(
    date: LocalDateTime?,
    onDateChange: (LocalDateTime) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dateTimeFormatter = remember { DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM) }
    var showPicker by rememberSaveable { mutableStateOf(false) }
    val editable = date != null

    DisableSelection {
        TextField(
            value = date?.format(dateTimeFormatter) ?: "",
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
    AndroidThreeTen.init(LocalContext.current)

    var date by remember { mutableStateOf(LocalDateTime.now()) }

    ArcaeaOfflineTheme {
        NullableDateTimeEditor(date = date, onDateChange = { date = it })
    }
}
