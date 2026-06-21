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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.database.entities.calculatePotential
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme

@Composable
fun PlayRatingCalculator(
    modifier: Modifier = Modifier,
    score: Int = 0,
    constant: Int = 0,
    isConstantReadonly: Boolean = true,
) {
    val scoreTextFieldState = rememberArcaeaScoreTextFieldState(score)
    val constantTextFieldState = rememberArcaeaConstantStepperTextFieldState(constant / 10.0)

    LaunchedEffect(score) {
        scoreTextFieldState.updateValue(score)
    }

    LaunchedEffect(constant) {
        constantTextFieldState.commitValue(constant.toBigDecimal())
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

            calculatePotential(scoreValue!!, constantValue!!)
        }
    }

    Column(modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedArcaeaScoreTextField(
                scoreTextFieldState,
                Modifier.weight(1f),
            )

            DecimalStepperTextField(
                constantTextFieldState,
                modifier = Modifier.weight(1f),
                label = { Text(stringResource(R.string.arcaea_constant)) },
                readonly = isConstantReadonly,
            )
        }

        Row(
            Modifier.padding(vertical = 16.dp),
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
