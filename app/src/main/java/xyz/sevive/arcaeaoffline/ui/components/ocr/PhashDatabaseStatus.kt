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
import xyz.sevive.arcaeaoffline.helpers.GlobalOcrDependencyHelper


@Composable
fun OcrDependencyPhashDatabaseStatus(
    state: GlobalOcrDependencyHelper.PhashDatabaseState, modifier: Modifier = Modifier
) {
    val db = state.database
    val exception = state.exception

    val status = if (exception == null) OcrDependencyStatus.OK else OcrDependencyStatus.ERROR

    val summary: String = if (exception != null) {
        exception::class.simpleName ?: "Error"
    } else if (db != null) {
        buildString {
            append("J${db.jacketHashes.size} PI${db.partnerIconHashes.size}")
            db.builtTime?.let {
                append('@')
                append(
                    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                        .format(LocalDateTime.ofInstant(it, ZoneId.systemDefault()))
                )
            }
        }
    } else "Unknown status"

    val detail = if (exception != null) {
        exception.message ?: exception.toString()
    } else null


    OcrDependencyItemStatus(
        title = { Text(stringResource(R.string.ocr_dependency_phash_database)) },
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
