package xyz.sevive.arcaeaoffline.desktop

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import xyz.sevive.arcaeaoffline.core.calculators.calculatePlayRating

fun main() =
    application {
        Window(onCloseRequest = ::exitApplication, title = "Arcaea Offline - Play Rating Calculator") {
            CalculatorScreen()
        }
    }

@Composable
fun CalculatorScreen() {
    var scoreText by remember { mutableStateOf("") }
    var constantText by remember { mutableStateOf("") }

    val score = scoreText.toIntOrNull() ?: 0
    val constant = (constantText.toDoubleOrNull() ?: 0.0)
    val result = calculatePlayRating(score, (constant * 10).toInt())

    MaterialTheme {
        Column(Modifier.padding(16.dp).width(400.dp)) {
            OutlinedTextField(scoreText, { scoreText = it }, label = { Text("Score") })
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(constantText, { constantText = it }, label = { Text("Constant (e.g. 10.7)") })
            Spacer(Modifier.height(16.dp))
            Text("Play Rating: ${"%.4f".format(result)}")
        }
    }
}
