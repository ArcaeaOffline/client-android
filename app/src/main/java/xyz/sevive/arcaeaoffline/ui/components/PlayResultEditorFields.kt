package xyz.sevive.arcaeaoffline.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultClearType
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultModifier
import xyz.sevive.arcaeaoffline.ui.common.datetimeeditor.NullableDateTimeEditor
import kotlin.time.Clock
import kotlin.time.Instant

@Composable
internal fun <T : Any> rememberLastNotNullValue(
    value: T?,
    defaultValueConstructor: () -> T,
): T {
    var lastNotNullValue by rememberSaveable { mutableStateOf(defaultValueConstructor()) }

    LaunchedEffect(value) {
        value?.let { lastNotNullValue = it }
    }

    return lastNotNullValue
}

@Composable
internal fun SetNullCheckbox(
    isNull: Boolean,
    onIsNullChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = isNull,
            onCheckedChange = onIsNullChange,
        )
        Text(stringResource(R.string.play_result_editor_set_null))
    }
}

@Composable
internal fun <T : Any> GeneralNullableFieldWrapper(
    value: T?,
    defaultValueConstructor: () -> T,
    onValueChange: (T?) -> Unit,
    modifier: Modifier = Modifier,
    valueComponent: @Composable RowScope.() -> Unit,
) {
    val lastNotNullValue =
        rememberLastNotNullValue(
            value = value,
            defaultValueConstructor = defaultValueConstructor,
        )

    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        valueComponent()

        SetNullCheckbox(isNull = value == null, onIsNullChange = {
            onValueChange(if (it) null else lastNotNullValue)
        })
    }
}

@Composable
internal fun NullableNumberInputWithCheckboxWrapper(
    value: Int?,
    onValueChange: (Int?) -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    GeneralNullableFieldWrapper(
        value = value,
        defaultValueConstructor = { 0 },
        onValueChange = onValueChange,
        modifier = modifier,
    ) {
        NullableNumberInput(
            value = value,
            onValueChange = onValueChange,
            label = label,
            minimum = 0,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
internal fun PlayResultEditorPureField(
    pure: Int?,
    onPureChange: (Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    NullableNumberInputWithCheckboxWrapper(
        value = pure,
        onValueChange = { onPureChange(it) },
        label = { Text(stringResource(R.string.arcaea_play_result_pure)) },
        modifier = modifier,
    )
}

@Composable
internal fun PlayResultEditorFarField(
    far: Int?,
    onFarChange: (Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    NullableNumberInputWithCheckboxWrapper(
        value = far,
        onValueChange = { onFarChange(it) },
        label = { Text(stringResource(R.string.arcaea_play_result_far)) },
        modifier = modifier,
    )
}

@Composable
internal fun PlayResultEditorLostField(
    lost: Int?,
    onLostChange: (Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    NullableNumberInputWithCheckboxWrapper(
        value = lost,
        onValueChange = { onLostChange(it) },
        label = { Text(stringResource(R.string.arcaea_play_result_lost)) },
        modifier = modifier,
    )
}

@Composable
internal fun PlayResultEditorMaxRecallField(
    maxRecall: Int?,
    onMaxRecallChange: (Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    NullableNumberInputWithCheckboxWrapper(
        value = maxRecall,
        onValueChange = { onMaxRecallChange(it) },
        label = { Text(stringResource(R.string.arcaea_play_result_max_recall)) },
        modifier = modifier,
    )
}

@Composable
internal fun PlayResultEditorDateTimeField(
    instant: Instant?,
    onInstantChange: (Instant?) -> Unit,
    modifier: Modifier = Modifier,
) {
    // The initial release date of Arcaea
    val minDate = remember { LocalDate(2017, 3, 7) }

    GeneralNullableFieldWrapper(
        value = instant,
        defaultValueConstructor = { Clock.System.now() },
        onValueChange = onInstantChange,
        modifier = modifier,
    ) {
        NullableDateTimeEditor(
            dateTime = instant?.toLocalDateTime(TimeZone.currentSystemDefault()),
            onDateTimeChange = {
                onInstantChange(it.toInstant(TimeZone.currentSystemDefault()))
            },
            modifier = Modifier.weight(1f),
            minDate = minDate,
        )
    }
}

@Composable
internal fun PlayResultEditorClearTypeField(
    clearType: ArcaeaPlayResultClearType?,
    onClearTypeChange: (ArcaeaPlayResultClearType?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val values = remember { ArcaeaPlayResultClearType.entries.sortedBy { it.value } }
    val labels = remember { values.map { AnnotatedString(it.toDisplayString()) } }
    val textFieldValue = remember(clearType) { clearType?.toDisplayString() ?: "" }

    var showSelectDialog by rememberSaveable { mutableStateOf(false) }
    if (showSelectDialog) {
        SelectDialog(
            labels = labels,
            onDismiss = { showSelectDialog = false },
            onSelect = { onClearTypeChange(values[it]) },
            selectedOptionIndex = clearType?.value,
        )
    }

    val editable = remember(clearType) { clearType != null }
    val tryShowSelectDialog =
        remember(editable) {
            { if (editable) showSelectDialog = true }
        }

    GeneralNullableFieldWrapper(
        value = clearType,
        defaultValueConstructor = { ArcaeaPlayResultClearType.NORMAL_CLEAR },
        onValueChange = onClearTypeChange,
        modifier = modifier,
    ) {
        TextField(
            value = textFieldValue,
            onValueChange = {},
            modifier =
                Modifier
                    .clickable(editable) { tryShowSelectDialog() }
                    .weight(1f),
            readOnly = true,
            enabled = editable,
            label = { Text(stringResource(R.string.arcaea_play_result_clear_type)) },
            trailingIcon = {
                IconButton(onClick = { tryShowSelectDialog() }, enabled = editable) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                }
            },
        )
    }
}

@Composable
internal fun PlayResultEditorModifierField(
    arcaeaModifier: ArcaeaPlayResultModifier?,
    onArcaeaModifierChange: (ArcaeaPlayResultModifier?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val values = remember { ArcaeaPlayResultModifier.entries.sortedBy { it.value } }
    val labels = remember { values.map { AnnotatedString(it.toDisplayString()) } }
    val textFieldValue = remember(arcaeaModifier) { arcaeaModifier?.toDisplayString() ?: "" }

    var showSelectDialog by rememberSaveable { mutableStateOf(false) }
    if (showSelectDialog) {
        SelectDialog(
            labels = labels,
            onDismiss = { showSelectDialog = false },
            onSelect = { onArcaeaModifierChange(values[it]) },
            selectedOptionIndex = arcaeaModifier?.value,
        )
    }

    val editable = remember(arcaeaModifier) { arcaeaModifier != null }
    val tryShowSelectDialog =
        remember(editable) {
            { if (editable) showSelectDialog = true }
        }

    GeneralNullableFieldWrapper(
        value = arcaeaModifier,
        defaultValueConstructor = { ArcaeaPlayResultModifier.NORMAL },
        onValueChange = onArcaeaModifierChange,
        modifier = modifier,
    ) {
        TextField(
            value = textFieldValue,
            onValueChange = {},
            modifier =
                Modifier
                    .clickable(editable) { tryShowSelectDialog() }
                    .weight(1f),
            readOnly = true,
            enabled = editable,
            label = { Text(stringResource(R.string.arcaea_play_result_modifier)) },
            trailingIcon = {
                IconButton(onClick = { tryShowSelectDialog() }, enabled = editable) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                }
            },
        )
    }
}

@Composable
internal fun PlayResultEditorCommentField(
    comment: String?,
    onCommentChange: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    GeneralNullableFieldWrapper(
        value = comment,
        defaultValueConstructor = { "" },
        onValueChange = onCommentChange,
        modifier = modifier,
    ) {
        TextField(
            value = comment ?: "",
            onValueChange = { onCommentChange(it) },
            modifier = Modifier.weight(1f),
            enabled = comment != null,
            label = { Text(stringResource(R.string.play_result_editor_comment_field)) },
        )
    }
}
