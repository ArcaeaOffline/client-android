package xyz.sevive.arcaeaoffline.ui.activities.ocrfromshare

import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import xyz.sevive.arcaeaoffline.ui.components.ocr.OcrDependencyKnnModelStatus
import xyz.sevive.arcaeaoffline.ui.components.ocr.OcrDependencyPhashDatabaseStatus
import xyz.sevive.arcaeaoffline.ui.models.OcrDependencyViewModel


@Composable
internal fun OcrFromShareOcrDependencyStatusCard(
    ocrDependencyViewModel: OcrDependencyViewModel,
    modifier: Modifier = Modifier,
) {
    val knnModelState by ocrDependencyViewModel.knnModelState.collectAsStateWithLifecycle()
    val phashDatabaseState by ocrDependencyViewModel.phashDatabaseState.collectAsStateWithLifecycle()

    Card(modifier) {
        OcrDependencyKnnModelStatus(state = knnModelState)
        OcrDependencyPhashDatabaseStatus(state = phashDatabaseState)
    }
}
