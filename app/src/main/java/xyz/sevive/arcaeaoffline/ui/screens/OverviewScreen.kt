package xyz.sevive.arcaeaoffline.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun OverviewScreen(modifier: Modifier = Modifier) {
    Text("1", modifier.fillMaxSize(), style = MaterialTheme.typography.displayLarge)
}
