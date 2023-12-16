package xyz.sevive.arcaeaoffline.ui.ocr

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun OcrScreen(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize()) {
        OcrQueueScreen()
    }
}
