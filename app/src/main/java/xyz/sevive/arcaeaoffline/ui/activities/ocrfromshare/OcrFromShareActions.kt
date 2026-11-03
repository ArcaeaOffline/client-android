package xyz.sevive.arcaeaoffline.ui.activities.ocrfromshare

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.helpers.ArcaeaPlayResultValidator
import xyz.sevive.arcaeaoffline.permissions.storage.SaveImageToGallery
import xyz.sevive.arcaeaoffline.ui.activities.ocrfromshare.OcrFromShareViewModel.OcrResultState
import xyz.sevive.arcaeaoffline.ui.common.PermissionRequiredDialog
import xyz.sevive.arcaeaoffline.ui.components.IconRow
import xyz.sevive.arcaeaoffline.ui.components.SaveWithWarningsDialog
import xyz.sevive.arcaeaoffline.ui.theme.spacing

@Composable
internal fun OcrFromShareActions(ocrFromShareViewModel: OcrFromShareViewModel) {
    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current

    val handleSavePicture = { ocrFromShareViewModel.saveImage() }
    val requestWriteStoragePermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission(),
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
            permissions = listOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
        )
    }

    val resultState by ocrFromShareViewModel.ocrResultState.collectAsStateWithLifecycle()
    val playResult = (resultState as? OcrResultState.Done)?.playResult
    val imageSaved by ocrFromShareViewModel.imageSaved.collectAsStateWithLifecycle()
    val saveState by ocrFromShareViewModel.playResultSaveState.collectAsStateWithLifecycle()
    val display by ocrFromShareViewModel.display.collectAsStateWithLifecycle()
    val warnings =
        remember(playResult, display) {
            playResult?.let { ArcaeaPlayResultValidator.validate(playResult = it, chartInfo = display?.chartInfo) }.orEmpty()
        }

    var showSaveWithWarningsDialog by rememberSaveable { mutableStateOf(false) }
    if (showSaveWithWarningsDialog) {
        SaveWithWarningsDialog(
            warnings = warnings,
            onConfirm = {
                showSaveWithWarningsDialog = false
                coroutineScope.launch { ocrFromShareViewModel.savePlayResult() }
            },
            onDismissRequest = { showSaveWithWarningsDialog = false },
        )
    }

    Column {
        Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
            Button(
                onClick = {
                    if (warnings.isEmpty()) {
                        coroutineScope.launch { ocrFromShareViewModel.savePlayResult() }
                    } else {
                        showSaveWithWarningsDialog = true
                    }
                },
                enabled = playResult != null && !saveState.saved && !saveState.cached,
            ) {
                AnimatedContent(saveState.saved) {
                    if (it) {
                        IconRow {
                            Icon(Icons.Default.Check, null)
                            Text(stringResource(R.string.ocr_from_share_play_result_saved))
                        }
                    } else {
                        IconRow {
                            Icon(Icons.Default.Save, null)
                            Text(stringResource(R.string.general_save))
                        }
                    }
                }
            }

            OutlinedButton(
                onClick = {
                    // TODO: coroutineScope.launch { ocrFromShareViewModel.cachePlayResult(context) }
                    Toast.makeText(context, "TODO", Toast.LENGTH_SHORT).show()
                },
                enabled = playResult != null && !saveState.saved && !saveState.cached,
            ) {
                IconRow {
                    Icon(Icons.Default.Archive, null)
                    Text(stringResource(R.string.ocr_from_share_cache_play_result_button))
                }
            }
        }

        OutlinedButton(
            onClick = {
                if (SaveImageToGallery.checkPermission(context)) {
                    handleSavePicture()
                } else {
                    showSavePicturePermissionRequiredDialog = true
                }
            },
            enabled = !imageSaved,
            colors =
                ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.secondary,
                ),
        ) {
            IconRow {
                Icon(Icons.Default.FileDownload, null)
                Text(stringResource(R.string.ocr_from_share_save_image_button))
            }
        }
    }
}
