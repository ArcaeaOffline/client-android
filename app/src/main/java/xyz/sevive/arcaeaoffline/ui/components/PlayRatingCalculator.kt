package xyz.sevive.arcaeaoffline.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.calculators.calculatePlayRating
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultClearType
import xyz.sevive.arcaeaoffline.ui.components.arcaea.OutlinedArcaeaScoreTextField
import xyz.sevive.arcaeaoffline.ui.components.arcaea.rememberArcaeaScoreTextFieldState
import xyz.sevive.arcaeaoffline.ui.helpers.ArcaeaFormatters
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme

@Composable
fun PlayRatingCalculator(
    modifier: Modifier = Modifier,
    score: Int = 0,
    constant: Int = 0,
    isConstantReadonly: Boolean = true,
    initialFocusScoreTextField: Boolean = false,
) {
    val scoreTextFieldFocusRequester = remember { FocusRequester() }

    // No clear type means no clear bonus (same as TRACK_LOST)
    var clearType by remember { mutableStateOf<ArcaeaPlayResultClearType?>(null) }

    var showClearTypeSelectDialog by rememberSaveable { mutableStateOf(false) }
    if (showClearTypeSelectDialog) {
        val values = remember { ArcaeaPlayResultClearType.entries.sortedBy { it.value } }
        SelectDialog(
            // Index 0 is the no-clear-type option; the entries follow from index 1
            labels =
                buildList {
                    add(AnnotatedString(stringResource(R.string.play_result_no_clear_type)))
                    values.forEach { add(AnnotatedString(it.toDisplayString())) }
                },
            onDismiss = { showClearTypeSelectDialog = false },
            onSelect = {
                clearType = if (it == 0) null else values[it - 1]
                showClearTypeSelectDialog = false
            },
            selectedOptionIndex = clearType?.let { values.indexOf(it) + 1 },
        )
    }

    val scoreTextFieldState =
        rememberArcaeaScoreTextFieldState(
            initialValue = score,
            initialSelectAll = initialFocusScoreTextField,
        )
    val constantTextFieldState = rememberArcaeaConstantStepperTextFieldState(constant / 10.0)

    LaunchedEffect(score) {
        scoreTextFieldState.updateValue(score)
    }

    LaunchedEffect(constant) {
        constantTextFieldState.commitValue(constant / 10.0)
    }

    LaunchedEffect(initialFocusScoreTextField) {
        if (initialFocusScoreTextField) scoreTextFieldFocusRequester.requestFocus()
    }

    val scoreValue by remember {
        derivedStateOf { scoreTextFieldState.intValue }
    }
    val constantValue by remember {
        derivedStateOf {
            constantTextFieldState.value
                ?.let { it * 10 }
                ?.intValue()
        }
    }

    val potential by remember {
        derivedStateOf {
            scoreValue ?: return@derivedStateOf null
            constantValue ?: return@derivedStateOf null

            calculatePlayRating(scoreValue!!, constantValue!!)
        }
    }

    Column(modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedArcaeaScoreTextField(
                scoreTextFieldState,
                Modifier
                    .weight(1f)
                    .focusRequester(scoreTextFieldFocusRequester),
            )

            DecimalStepperTextField(
                constantTextFieldState,
                modifier = Modifier.weight(1f),
                label = { Text(stringResource(R.string.arcaea_constant)) },
                readonly = isConstantReadonly,
            )
        }

        Row(
            Modifier.padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowRight, contentDescription = null)

            Text(
                potential?.let { String.format(null, "%.4f", it) } ?: "?",
                Modifier.weight(1f),
                style = MaterialTheme.typography.titleLarge,
            )
        }
    }
}

@Preview
@Composable
private fun PlayRatingCalculatorPreview() {
    ArcaeaOfflineTheme {
        Surface {
            PlayRatingCalculator()
        }
    }
}
