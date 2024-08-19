package xyz.sevive.arcaeaoffline.ui.components.ocr

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.ocr.ImageHashesDatabaseStatusDetail
import xyz.sevive.arcaeaoffline.ui.components.LinearProgressIndicatorWrapper


data class OcrDependencyImageHashesDatabaseStatusUiState(
    val progress: Pair<Int, Int>? = null,
    val statusDetail: ImageHashesDatabaseStatusDetail = ImageHashesDatabaseStatusDetail(),
)

@Composable
fun OcrDependencyImageHashesDatabaseStatusViewer(
    uiState: OcrDependencyImageHashesDatabaseStatusUiState, modifier: Modifier = Modifier
) {
    val statusDetail = remember(uiState) { uiState.statusDetail }

    val title = stringResource(R.string.ocr_dependency_image_hashes_database)
    val status = remember(statusDetail) { statusDetail.status() }
    val summary = remember(statusDetail) { statusDetail.summary() }
    val details = remember(statusDetail) { statusDetail.details() }

    if (uiState.progress != null) {
        OcrDependencyStatusViewer(
            title = title,
            status = status,
            summary = {
                Box(Modifier.padding(vertical = 2.dp)) {
                    LinearProgressIndicatorWrapper(
                        progress = uiState.progress, Modifier.fillMaxWidth(0.8f)
                    )
                }
            },
            details = details,
            modifier = modifier,
        )
    } else {
        OcrDependencyStatusViewer(
            title = title,
            status = status,
            summary = summary,
            details = details,
            modifier = modifier
        )
    }
}
