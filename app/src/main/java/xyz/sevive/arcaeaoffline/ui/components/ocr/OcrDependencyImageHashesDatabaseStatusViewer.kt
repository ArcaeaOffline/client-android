package xyz.sevive.arcaeaoffline.ui.components.ocr

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
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

    val icon = ImageVector.vectorResource(R.drawable.ic_database_hashes)
    val title = stringResource(R.string.ocr_dependency_image_hashes_database)
    val status = remember(statusDetail) { statusDetail.status() }
    val summary = remember(statusDetail) { statusDetail.summary() }
    val details = remember(statusDetail) { statusDetail.details() }

    if (uiState.progress != null) {
        OcrDependencyStatusViewer(
            icon = icon,
            title = title,
            status = status,
            summary = { LinearProgressIndicatorWrapper(progress = uiState.progress) },
            details = details,
            modifier = modifier,
        )
    } else {
        OcrDependencyStatusViewer(
            icon = icon,
            title = title,
            status = status,
            summary = summary,
            details = details,
            modifier = modifier
        )
    }
}
