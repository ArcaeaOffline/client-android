package xyz.sevive.arcaeaoffline.ui.ocr

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import xyz.sevive.arcaeaoffline.ui.SubScreenContainer
import xyz.sevive.arcaeaoffline.ui.ocr.queue.OcrQueue
import xyz.sevive.arcaeaoffline.ui.ocr.queue.OcrQueueViewModel


@Composable
fun OcrQueueScreen(
    ocrQueueViewModel: OcrQueueViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val tasks by ocrQueueViewModel.ocrQueueTasks.collectAsState()
    val queueRunning by ocrQueueViewModel.queueRunning.collectAsState()

    val pickImagesLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        coroutineScope.launch { ocrQueueViewModel.addImageFiles(uris, context) }
    }

    val folderLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            val folder = DocumentFile.fromTreeUri(context, uri)
            folder?.let { coroutineScope.launch { ocrQueueViewModel.addFolder(it, context) } }
        }
    }

    val addImagesProgress by ocrQueueViewModel.addImagesProgress.collectAsState()
    val addImagesProgressTotal by ocrQueueViewModel.addImagesProgressTotal.collectAsState()
    if (addImagesProgressTotal > -1) {
        AlertDialog(
            onDismissRequest = {},
            confirmButton = {},
            icon = { Icon(Icons.Default.PhotoLibrary, null) },
            text = {
                Column {
                    Text("$addImagesProgress/$addImagesProgressTotal")
                    LinearProgressIndicator(progress = { addImagesProgress.toFloat() / addImagesProgressTotal })
                }
            },
        )
    }

    SubScreenContainer(onNavigateUp = { /*TODO*/ }, title = { /*TODO*/ }) {
        Column {
            Row {
                Button(
                    onClick = {
                        pickImagesLauncher.launch("image/*")
                    },
                    enabled = !queueRunning,
                ) {
                    Text("Add")
                }

                Button(
                    onClick = { folderLauncher.launch(null) },
                    enabled = !queueRunning,
                ) {
                    Text("Select")
                }
            }

            OcrQueue(ocrQueueViewModel)
        }
    }
}
