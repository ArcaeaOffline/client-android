package xyz.sevive.arcaeaoffline.ui.components

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.calculators.PLAY_RATING_CLEAR_BONUS
import xyz.sevive.arcaeaoffline.core.calculators.calculatePlayRating
import xyz.sevive.arcaeaoffline.ui.components.arcaea.OutlinedArcaeaScoreTextField
import xyz.sevive.arcaeaoffline.ui.components.arcaea.rememberArcaeaScoreTextFieldState
import xyz.sevive.arcaeaoffline.ui.components.preferences.SwitchPreferencesWidget
import xyz.sevive.arcaeaoffline.ui.helpers.ArcaeaFormatters
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme

@Composable
fun PlayRatingCalculator(
    modifier: Modifier = Modifier,
    score: Int = 0,
    constant: Int = 0,
    isConstantReadonly: Boolean = true,
    initialFocusScoreTextField: Boolean = false,
    initialCleared: Boolean = false,
    countClearBonus: Boolean = true,
) {
    val scoreTextFieldFocusRequester = remember { FocusRequester() }

    var cleared by rememberSaveable { mutableStateOf(initialCleared) }

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

            val clearBonus = if (countClearBonus && cleared) PLAY_RATING_CLEAR_BONUS else 0.0
            calculatePlayRating(scoreValue!!, constantValue!!, clearBonus)
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

            // A tool output: shown to 6 decimals, independent of the official
            // display precision
            Text(
                potential?.let { ArcaeaFormatters.potentialToText(it, scale = 6) } ?: "?",
                Modifier.weight(1f),
                style = MaterialTheme.typography.titleLarge,
            )
        }

        if (countClearBonus) {
            SwitchPreferencesWidget(
                value = cleared,
                onValueChange = { cleared = it },
                title = stringResource(R.string.play_rating_cleared),
                description = stringResource(R.string.play_rating_cleared_description),
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
