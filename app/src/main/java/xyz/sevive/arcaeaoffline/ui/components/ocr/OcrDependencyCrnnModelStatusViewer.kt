package xyz.sevive.arcaeaoffline.ui.components.ocr

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.ocr.CrnnModelStatusDetail

data class OcrDependencyCrnnModelStatusUiState(
    val statusDetail: CrnnModelStatusDetail = CrnnModelStatusDetail(),
)

@Composable
fun OcrDependencyCrnnModelStatusViewer(
    uiState: OcrDependencyCrnnModelStatusUiState, modifier: Modifier = Modifier
) {
    val statusDetail = uiState.statusDetail

    val status = remember(statusDetail) { statusDetail.status() }
    val summary = remember(statusDetail) { statusDetail.summary() }
    val details = remember(statusDetail) { statusDetail.details() }

    OcrDependencyStatusViewer(
        title = stringResource(R.string.ocr_dependency_crnn_model),
        status = status,
        summary = summary,
        details = details,
        modifier = modifier,
    )
}
