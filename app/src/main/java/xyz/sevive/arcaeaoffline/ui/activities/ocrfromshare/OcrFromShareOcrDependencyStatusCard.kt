package xyz.sevive.arcaeaoffline.ui.activities.ocrfromshare

import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import xyz.sevive.arcaeaoffline.ui.components.ocr.OcrDependencyCrnnModelStatusViewer
import xyz.sevive.arcaeaoffline.ui.components.ocr.OcrDependencyImageHashesDatabaseStatusViewer
import xyz.sevive.arcaeaoffline.ui.components.ocr.OcrDependencyKNearestModelStatusViewer


@Composable
internal fun OcrFromShareOcrDependencyStatusCard(
    uiState: OcrFromShareViewModel.OcrDependencyViewersUiState,
    modifier: Modifier = Modifier
) {
    Card(modifier) {
        OcrDependencyKNearestModelStatusViewer(uiState = uiState.kNearestModel)
        OcrDependencyImageHashesDatabaseStatusViewer(uiState = uiState.imageHashesDatabase)
        OcrDependencyCrnnModelStatusViewer(uiState = uiState.crnnModel)
    }
}
