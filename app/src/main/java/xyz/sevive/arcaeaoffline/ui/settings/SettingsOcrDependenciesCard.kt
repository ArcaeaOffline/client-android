package xyz.sevive.arcaeaoffline.ui.settings

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Api
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.data.OcrDependencyPaths
import xyz.sevive.arcaeaoffline.ui.components.ActionCard
import xyz.sevive.arcaeaoffline.ui.components.TitleOutlinedCard
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
fun SettingsOcrDependenciesCard(viewModel: OcrDependencyViewModel = viewModel()) {
    var expanded by rememberSaveable { mutableStateOf(true) }
    val expandArrowRotateDegree: Float by animateFloatAsState(
        if (expanded) 0f else -90f, label = "expandArrowRotate"
    )

    val context = LocalContext.current
    val knnModelState = viewModel.knnModelState.collectAsState()
    val phashDatabaseState = viewModel.phashDatabaseState.collectAsState()

    val ocrDependencyPaths = OcrDependencyPaths(context)
    viewModel.setOcrDependencyPaths(ocrDependencyPaths)
    viewModel.reload()

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

            viewModel.reload()
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

            viewModel.reload()
        }
    }

    TitleOutlinedCard(title = {
        ActionCard(onClick = { expanded = !expanded },
            title = stringResource(R.string.settings_ocr_dependencies_title),
            shape = settingsTitleActionCardShape(),
            headSlot = { Icon(Icons.Default.Api, null) },
            tailSlot = {
                Icon(
                    Icons.Filled.ExpandMore, null, Modifier.rotate(expandArrowRotateDegree)
                )
            })
    }) { padding ->
        AnimatedVisibility(expanded) {
            Column(Modifier.padding(padding)) {
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
