package xyz.sevive.arcaeaoffline.ui.activities.ocrfromshare

import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import xyz.sevive.arcaeaoffline.helpers.GlobalOcrDependencyHelper
import xyz.sevive.arcaeaoffline.ui.components.ocr.OcrDependencyKnnModelStatus
import xyz.sevive.arcaeaoffline.ui.components.ocr.OcrDependencyPhashDatabaseStatus


@Composable
internal fun OcrFromShareOcrDependencyStatusCard(modifier: Modifier = Modifier) {
    val knnModelState by GlobalOcrDependencyHelper.knnModelState.collectAsStateWithLifecycle()
    val phashDatabaseState by GlobalOcrDependencyHelper.phashDatabaseState.collectAsStateWithLifecycle()

    Card(modifier) {
        OcrDependencyKnnModelStatus(state = knnModelState)
        OcrDependencyPhashDatabaseStatus(state = phashDatabaseState)
    }
}
