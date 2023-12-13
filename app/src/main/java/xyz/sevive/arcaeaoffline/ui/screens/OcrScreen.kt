package xyz.sevive.arcaeaoffline.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun OcrScreen(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize()) {
        Text("3", style = MaterialTheme.typography.displayLarge)
    }
}
