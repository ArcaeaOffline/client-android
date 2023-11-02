package xyz.sevive.arcaeaoffline.ui.components.ocr

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.models.PhashDatabaseState


@Composable
fun OcrDependencyPhashDatabaseStatus(
    state: PhashDatabaseState, modifier: Modifier = Modifier
) {
    val db = state.db
    val error = state.error

    OcrDependencyItemStatus(
        title = { Text(stringResource(R.string.ocr_dependency_phash_database)) },
        label = {
            if (error == null && db != null) {
                Text(
                    "J${db.jacketHashes.size} PI${db.partnerIconHashes.size}",
                    modifier = modifier,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        },
        status = if (error == null) OcrDependencyStatus.OK else OcrDependencyStatus.ERROR,
        details = if (error != null) {
            {
                Text(
                    error.message ?: error.toString(),
                    modifier = modifier,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        } else null,
        modifier = modifier)
}
