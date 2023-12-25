package xyz.sevive.arcaeaoffline.ui.activities.ocrfromshare

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.permissions.storage.SaveBitmapToGallery
import xyz.sevive.arcaeaoffline.ui.common.PermissionRequiredDialog
import xyz.sevive.arcaeaoffline.ui.components.IconRow


@Composable
internal fun OcrFromShareActions(ocrFromShareViewModel: OcrFromShareViewModel) {
    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current

    val handleSavePicture = { ocrFromShareViewModel.saveImage(context) }
    val requestWriteStoragePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { if (it) handleSavePicture() }

    var showSavePicturePermissionRequiredDialog by rememberSaveable { mutableStateOf(false) }
    if (showSavePicturePermissionRequiredDialog) {
        PermissionRequiredDialog(
            onDismiss = { showSavePicturePermissionRequiredDialog = false },
            onConfirm = {
                requestWriteStoragePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                showSavePicturePermissionRequiredDialog = false
            },
            functionName = stringResource(R.string.permission_function_save_image),
            permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        )
    }

    val score by ocrFromShareViewModel.score.collectAsState()
    val scoreSaved by ocrFromShareViewModel.scoreSaved.collectAsState()
    val imageSaved by ocrFromShareViewModel.imageSaved.collectAsState()
    val scoreCached by ocrFromShareViewModel.scoreCached.collectAsState()

    Column {
        Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_arrangement_padding))) {
            Button(
                onClick = { coroutineScope.launch { ocrFromShareViewModel.saveScore() } },
                enabled = score != null && !scoreSaved && !scoreCached,
            ) {
                IconRow(icon = { Icon(Icons.Default.Save, null) }) {
                    Text(stringResource(R.string.general_save))
                }
            }

            OutlinedButton(
                onClick = { coroutineScope.launch { ocrFromShareViewModel.cacheScore(context) } },
                enabled = score != null && !scoreSaved && !scoreCached,
            ) {
                IconRow(icon = { Icon(Icons.Default.Archive, null) }) {
                    Text(stringResource(R.string.ocr_from_share_cache_score_button))
                }
            }
        }

        OutlinedButton(
            onClick = {
                if (SaveBitmapToGallery.checkPermission(context)) handleSavePicture()
                else showSavePicturePermissionRequiredDialog = true
            },
            enabled = !imageSaved,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.secondary
            ),
        ) {
            IconRow(icon = { Icon(Icons.Default.FileDownload, null) }) {
                Text(stringResource(R.string.ocr_from_share_save_image_button))
            }
        }
    }
}
