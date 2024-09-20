package xyz.sevive.arcaeaoffline.ui.screens.database.manage

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import org.threeten.bp.Instant
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.components.IconRow

private class CreateJsonDocument : ActivityResultContracts.CreateDocument("application/json")

@Composable
fun DatabaseManageExport(viewModel: DatabaseManageViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val exportScoreHandler = rememberLauncherForActivityResult(CreateJsonDocument()) {
        if (it != null) {
            val outputStream = context.contentResolver.openOutputStream(it)
            if (outputStream != null) {
                coroutineScope.launch {
                    viewModel.exportScores(outputStream)
                }
            }
        }
    }

    Column(modifier) {
        Button(
            onClick = {
                exportScoreHandler.launch(
                    "arcaea-offline-data-exchange-${Instant.now().toEpochMilli()}"
                )
            },
        ) {
            IconRow {
                Icon(Icons.Default.UploadFile, null)
                Text(stringResource(R.string.database_manage_export_play_results))
            }
        }
    }
}
