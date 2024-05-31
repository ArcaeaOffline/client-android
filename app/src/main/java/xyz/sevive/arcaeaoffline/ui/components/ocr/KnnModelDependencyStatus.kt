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
    modifier: Modifier = Modifier
) {
    val model = state.model
    val exception = state.exception

    val detailString: String? = if (exception != null) {
        exception.message ?: exception.toString()
    } else if (model != null && model.varCount == 0) {
        "Invalid model"
    } else null

    OcrDependencyItemStatus(
        title = { Text(stringResource(R.string.ocr_dependency_knn_model)) },
        label = {
            if (exception == null && model != null) {
                Text(
                    "varCount ${model.varCount}", modifier = modifier,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else if (exception != null) {
                Text(
                    exception::class.simpleName ?: "Error", modifier = modifier,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        status = if (exception != null) {
            OcrDependencyStatus.ERROR
        } else if (model != null && (model.varCount == 0 || !model.isTrained)) {
            OcrDependencyStatus.ERROR
        } else if (model != null && model.varCount == 81) {
            OcrDependencyStatus.OK
        } else if (model != null) {
            OcrDependencyStatus.WARN
        } else {
            OcrDependencyStatus.UNKNOWN
        },
        details = if (detailString != null) {
            {
                Text(
                    detailString,
                    modifier = modifier,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        } else null,
        modifier = modifier,
    )
}
