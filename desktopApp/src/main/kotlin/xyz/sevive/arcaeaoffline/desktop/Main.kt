package xyz.sevive.arcaeaoffline.desktop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.jetbrains.compose.resources.stringResource
import xyz.sevive.arcaeaoffline.core.calculators.calculatePlayRating
import xyz.sevive.arcaeaoffline.resources.Res
import xyz.sevive.arcaeaoffline.resources.arcaea_constant
import xyz.sevive.arcaeaoffline.resources.arcaea_play_rating
import xyz.sevive.arcaeaoffline.ui.components.DecimalStepperTextField
import xyz.sevive.arcaeaoffline.ui.components.arcaea.OutlinedArcaeaScoreTextField
import xyz.sevive.arcaeaoffline.ui.components.arcaea.rememberArcaeaScoreTextFieldState
import xyz.sevive.arcaeaoffline.ui.components.rememberArcaeaConstantStepperTextFieldState
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme

fun main() =
    application {
        Window(onCloseRequest = ::exitApplication, title = "Arcaea Offline (Proof-of-Concept Prototype)") {
            ArcaeaOfflineTheme {
                Surface(Modifier.fillMaxSize()) {
                    CalculatorScreen()
                }
            }
        }
    }

@Composable
fun CalculatorScreen() {
    val scoreTextFieldState = rememberArcaeaScoreTextFieldState(initialValue = 0)
    val constantTextFieldState = rememberArcaeaConstantStepperTextFieldState(initialValue = 0.0)

    val score by remember { derivedStateOf { scoreTextFieldState.intValue ?: 0 } }
    val constant by remember { derivedStateOf { ((constantTextFieldState.doubleValue ?: 0.0) * 10).toInt() } }
    val result = calculatePlayRating(score, constant)

    Column(
        Modifier.padding(16.dp).width(400.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedArcaeaScoreTextField(scoreTextFieldState)

        DecimalStepperTextField(
            constantTextFieldState,
            label = { Text(stringResource(Res.string.arcaea_constant)) },
        )

        OutlinedTextField(
            value = "%.4f".format(result),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(Res.string.arcaea_play_rating)) },
        )
    }
}
