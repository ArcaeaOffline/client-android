package xyz.sevive.arcaeaoffline.ui.components.ocr

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.helpers.GlobalOcrDependencyHelper


@Composable
fun OcrDependencyKnnModelStatus(
    state: GlobalOcrDependencyHelper.KnnModelState,
    modifier: Modifier = Modifier,
) {
    val model = state.model
    val exception = state.exception

    val status = if (exception != null) {
        OcrDependencyStatus.ERROR
    } else if (model == null) {
        OcrDependencyStatus.UNKNOWN
    } else if (model.varCount == 0 || !model.isTrained) {
        OcrDependencyStatus.ERROR
    } else if (model.varCount == 81) {
        OcrDependencyStatus.OK
    } else {
        OcrDependencyStatus.WARN
    }

    val summary = if (exception != null) {
        exception::class.simpleName ?: "Error"
    } else if (model != null) {
        "varCount ${model.varCount}"
    } else "Unknown status"

    val detail: String? = if (exception != null) {
        exception.message ?: exception.toString()
    } else if (model != null && model.varCount == 0) {
        "Invalid model"
    } else null

    OcrDependencyItemStatus(
        title = { Text(stringResource(R.string.ocr_dependency_knn_model)) },
        label = {
            Text(
                summary,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        status = status,
        details = if (detail != null) {
            {
                Text(
                    detail,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else null,
        modifier = modifier,
    )
}
