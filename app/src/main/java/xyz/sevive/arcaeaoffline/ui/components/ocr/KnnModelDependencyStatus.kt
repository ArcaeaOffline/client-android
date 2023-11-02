package xyz.sevive.arcaeaoffline.ui.components.ocr

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.models.KnnModelState

@Composable
fun OcrDependencyKnnModelStatus(state: KnnModelState, modifier: Modifier = Modifier) {
    val model = state.model
    val error = state.error

    val detailString: String? = if (error != null) {
        error.message ?: error.toString()
    } else if (model != null && model.varCount == 0) {
        "Invalid model"
    } else null

    OcrDependencyItemStatus(
        title = { Text(stringResource(R.string.ocr_dependency_knn_model)) },
        label = {
            if (error == null && model != null) {
                Text(
                    "varCount ${model.varCount}", modifier = modifier,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
        },
        status = if (error != null) {
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
