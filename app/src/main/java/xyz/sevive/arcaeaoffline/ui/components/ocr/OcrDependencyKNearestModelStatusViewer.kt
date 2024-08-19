package xyz.sevive.arcaeaoffline.ui.components.ocr

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.ocr.KNearestModelStatusDetail


data class OcrDependencyKNearestModelStatusUiState(
    val statusDetail: KNearestModelStatusDetail = KNearestModelStatusDetail(),
)

@Composable
fun OcrDependencyKNearestModelStatusViewer(
    uiState: OcrDependencyKNearestModelStatusUiState, modifier: Modifier = Modifier
) {
    val status = remember(uiState) { uiState.statusDetail.status() }
    val summary = remember(uiState) { uiState.statusDetail.summary() }
    val details = remember(uiState) { uiState.statusDetail.details() }

    OcrDependencyStatusViewer(
        title = stringResource(R.string.ocr_dependency_knn_model),
        status = status,
        summary = summary,
        details = details,
        modifier = modifier,
    )
}
