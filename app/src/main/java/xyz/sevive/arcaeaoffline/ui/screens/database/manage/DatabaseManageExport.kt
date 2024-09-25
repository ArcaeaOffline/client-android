package xyz.sevive.arcaeaoffline.ui.screens.database.manage

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.threeten.bp.Instant
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.components.preferences.TextPreferencesWidget

private class CreateJsonDocument : ActivityResultContracts.CreateDocument("application/json")

@Composable
fun DatabaseManageExport(
    onExportPlayResults: (Uri) -> Unit,
    modifier: Modifier = Modifier,
) {
    val exportPlayResultsHandler = rememberLauncherForActivityResult(CreateJsonDocument()) {
        it?.let(onExportPlayResults)
    }

    Column(modifier) {
        TextPreferencesWidget(
            onClick = {
                exportPlayResultsHandler.launch(
                    "arcaea-offline-data-exchange-${Instant.now().toEpochMilli()}"
                )
            },
            title = stringResource(R.string.database_manage_export_play_results),
            leadingIcon = Icons.Default.UploadFile,
        )
    }
}
