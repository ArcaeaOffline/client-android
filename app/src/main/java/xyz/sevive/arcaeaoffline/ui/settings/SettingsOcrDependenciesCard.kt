package xyz.sevive.arcaeaoffline.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Api
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.data.OcrDependencyPaths
import xyz.sevive.arcaeaoffline.ui.components.ActionCard
import xyz.sevive.arcaeaoffline.ui.components.TitleOutlinedCard
import xyz.sevive.arcaeaoffline.ui.components.ocr.OcrDependencyKnnModelStatus
import xyz.sevive.arcaeaoffline.ui.components.ocr.OcrDependencyPhashDatabaseStatus
import xyz.sevive.arcaeaoffline.ui.models.OcrDependencyViewModel


@Composable
fun SettingsOcrDependenciesCard(
    settingsViewModel: SettingsViewModel,
    ocrDependencyViewModel: OcrDependencyViewModel = viewModel(),
) {
    var expanded by rememberSaveable { mutableStateOf(true) }
    val expandArrowRotateDegree: Float by animateFloatAsState(
        if (expanded) 0f else -90f, label = "expandArrowRotate"
    )

    var showBuildPhashDatabaseDialog by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val knnModelState = ocrDependencyViewModel.knnModelState.collectAsState()
    val phashDatabaseState = ocrDependencyViewModel.phashDatabaseState.collectAsState()

    val ocrDependencyPaths = OcrDependencyPaths(context)
    ocrDependencyViewModel.setOcrDependencyPaths(ocrDependencyPaths)
    ocrDependencyViewModel.reload()

    val importKnnLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) {
        it?.let {
            settingsViewModel.importKnnModel(
                context.contentResolver.openInputStream(it), ocrDependencyPaths,
            )
            ocrDependencyViewModel.reload()
        }
    }

    val importPhashDbLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) {
        it?.let {
            settingsViewModel.importPhashDatabase(
                context.contentResolver.openInputStream(it), ocrDependencyPaths,
            )
            ocrDependencyViewModel.reload()
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
                Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_arrangement_padding))) {
                    Button(onClick = { importPhashDbLauncher.launch("*/*") }) {
                        Text(stringResource(R.string.settings_ocr_import_phash_database))
                    }

                    Button(onClick = {
                        showBuildPhashDatabaseDialog = true
                        coroutineScope.launch {
                            settingsViewModel.buildPhashDatabaseFromArcaea(
                                context,
                                ocrDependencyPaths,
                            )
                            ocrDependencyViewModel.reload()
                            showBuildPhashDatabaseDialog = false
                        }
                    }) {
                        Text(stringResource(R.string.settings_ocr_phash_database_build_from_arcaea))
                    }
                }
            }
        }
    }

    if (showBuildPhashDatabaseDialog) {
        Dialog(onDismissRequest = {}) {
            Card {
                Column(Modifier.padding(dimensionResource(R.dimen.general_page_padding))) {
                    Text(stringResource(R.string.general_please_wait))
                    LinearProgressIndicator()
                }
            }
        }
    }
}
