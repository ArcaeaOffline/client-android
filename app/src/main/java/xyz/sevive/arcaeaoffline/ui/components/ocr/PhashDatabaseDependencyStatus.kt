package xyz.sevive.arcaeaoffline.ui.components.ocr

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
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
                var statusText = "J${db.jacketHashes.size} PI${db.partnerIconHashes.size}"

                if (db.builtTime != null) {
                    statusText += "@" + DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                        .format(
                            LocalDateTime.ofInstant(db.builtTime, ZoneId.systemDefault())
                        )
                }

                Text(
                    statusText,
                    modifier = modifier,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else if (error != null) {
                Text(
                    error::class.simpleName ?: "Error", modifier = modifier,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error,
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
