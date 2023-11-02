package xyz.sevive.arcaeaoffline.ui.screens

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.data.OcrDependencyPaths
import xyz.sevive.arcaeaoffline.ui.components.ocr.OcrDependencyKnnModelStatus
import xyz.sevive.arcaeaoffline.ui.components.ocr.OcrDependencyPhashDatabaseStatus
import xyz.sevive.arcaeaoffline.ui.models.OcrDependencyViewModel

fun mkOcrDependencyParentDirs(ocrDependencyPaths: OcrDependencyPaths) {
    if (!ocrDependencyPaths.parentDir.exists()) {
        if (!ocrDependencyPaths.parentDir.mkdirs()) {
            Log.w("OCR Dependency", "Cannot create dependencies parent directory")
        }
    }
}


@Composable
fun SettingsOcrDependencyCard(
    modifier: Modifier = Modifier, ocrDependencyViewModel: OcrDependencyViewModel = viewModel()
) {
    var expanded by rememberSaveable { mutableStateOf(true) }
    val expandArrowRotateDegree: Float by animateFloatAsState(
        if (expanded) 180f else 0f, label = "expandArrowRotate"
    )

    val context = LocalContext.current
    val knnModelState = ocrDependencyViewModel.knnModelState.collectAsState()
    val phashDatabaseState = ocrDependencyViewModel.phashDatabaseState.collectAsState()

    val ocrDependencyPaths = OcrDependencyPaths(context)
    ocrDependencyViewModel.setOcrDependencyPaths(ocrDependencyPaths)
    ocrDependencyViewModel.reload()

    val importKnnLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { fileUri ->
        if (fileUri != null) {
            mkOcrDependencyParentDirs(ocrDependencyPaths)
            val inputStream = context.contentResolver.openInputStream(fileUri)
            inputStream.use {
                val outputStream = ocrDependencyPaths.knnModelFile.outputStream()
                it?.copyTo(outputStream)
            }

            ocrDependencyViewModel.reload()
        }
    }

    val importPhashDbLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { fileUri ->
        if (fileUri != null) {
            mkOcrDependencyParentDirs(ocrDependencyPaths)
            val inputStream = context.contentResolver.openInputStream(fileUri)
            inputStream.use {
                val outputStream = ocrDependencyPaths.phashDatabaseFile.outputStream()
                it?.copyTo(outputStream)
            }

            ocrDependencyViewModel.reload()
        }
    }

    OutlinedCard(modifier.padding(16.dp)) {
        Row(
            modifier
                .clickable { expanded = !expanded }
                .padding(8.dp)) {
            Text(
                "OCR Dependencies",
                modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically),
                style = MaterialTheme.typography.titleLarge
            )

            TextButton(onClick = { expanded = !expanded }) {
                Icon(
                    Icons.Filled.ExpandMore,
                    "Expand",
                    modifier.rotate(expandArrowRotateDegree)
                )
            }
        }

        AnimatedVisibility(visible = expanded) {
            Column(modifier.padding(12.dp)) {
                OcrDependencyKnnModelStatus(state = knnModelState.value)
                Button(onClick = { importKnnLauncher.launch("*/*") }) {
                    Text(stringResource(R.string.settings_ocr_import_knn))
                }

                OcrDependencyPhashDatabaseStatus(state = phashDatabaseState.value)
                Button(onClick = { importPhashDbLauncher.launch("*/*") }) {
                    Text(stringResource(R.string.settings_ocr_import_phash_database))
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    SettingsOcrDependencyCard(modifier)
}
