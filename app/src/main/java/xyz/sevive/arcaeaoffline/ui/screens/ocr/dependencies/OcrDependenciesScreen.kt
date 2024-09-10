package xyz.sevive.arcaeaoffline.ui.screens.ocr.dependencies

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.helpers.GlobalArcaeaButtonStateHelper
import xyz.sevive.arcaeaoffline.helpers.rememberFileChooserLauncher
import xyz.sevive.arcaeaoffline.ui.AppViewModelProvider
import xyz.sevive.arcaeaoffline.ui.SubScreenContainer
import xyz.sevive.arcaeaoffline.ui.components.ArcaeaButton
import xyz.sevive.arcaeaoffline.ui.components.ArcaeaButtonState
import xyz.sevive.arcaeaoffline.ui.components.IconRow
import xyz.sevive.arcaeaoffline.ui.components.ocr.OcrDependencyCrnnModelStatusViewer
import xyz.sevive.arcaeaoffline.ui.components.ocr.OcrDependencyImageHashesDatabaseStatusViewer
import xyz.sevive.arcaeaoffline.ui.components.ocr.OcrDependencyKNearestModelStatusViewer
import xyz.sevive.arcaeaoffline.ui.navigation.OcrScreenDestinations

@Composable
fun OcrDependenciesScreen(
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OcrDependenciesScreenViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val context = LocalContext.current

    val kNearestModelUiState by viewModel.kNearestModelUiState.collectAsStateWithLifecycle()
    val imageHashesDatabaseUiState by viewModel.imageHashesDatabaseUiState.collectAsStateWithLifecycle()
    val crnnModelUiState by viewModel.crnnModelUiState.collectAsStateWithLifecycle()

    val buildHashesDatabaseArcaeaButtonState by GlobalArcaeaButtonStateHelper.buildHashesDatabaseButtonState.collectAsStateWithLifecycle()
    val buildHashesDatabaseButtonEnabled by viewModel.buildHashesDatabaseButtonEnabled.collectAsStateWithLifecycle()

    val kNearestModelFileChooserLauncher = rememberFileChooserLauncher { uri ->
        uri?.let { viewModel.importKNearestModel(it, context) }
    }
    val imageHashesDatabaseFileChooserLauncher = rememberFileChooserLauncher { uri ->
        uri?.let { viewModel.importImageHashesDatabase(it, context) }
    }

    SubScreenContainer(
        onNavigateUp = onNavigateUp,
        title = stringResource(OcrScreenDestinations.Dependencies.title),
        actions = {
            IconButton(onClick = { viewModel.reloadAll(context) }) {
                Icon(Icons.Default.Refresh, contentDescription = null)
            }
        },
    ) {
        LazyColumn(modifier) {
            item {
                Column {
                    OcrDependencyKNearestModelStatusViewer(kNearestModelUiState)

                    Button(onClick = { kNearestModelFileChooserLauncher.launch("*/*") }) {
                        IconRow(icon = { Icon(Icons.Default.FileOpen, null) }) {
                            Text(stringResource(R.string.general_import))
                        }
                    }
                }
            }

            item { HorizontalDivider(Modifier.padding(vertical = 2.dp)) }

            item {
                Column {
                    OcrDependencyImageHashesDatabaseStatusViewer(imageHashesDatabaseUiState)

                    Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_padding))) {
                        Button(onClick = { imageHashesDatabaseFileChooserLauncher.launch("*/*") }) {
                            IconRow(icon = { Icon(Icons.Default.FileOpen, null) }) {
                                Text(stringResource(R.string.general_import))
                            }
                        }

                        ArcaeaButton(
                            onClick = {
                                if (buildHashesDatabaseArcaeaButtonState == ArcaeaButtonState.NORMAL) viewModel.requestImageHashesDatabaseBuild()
                            },
                            state = buildHashesDatabaseArcaeaButtonState,
                            enabled = buildHashesDatabaseButtonEnabled,
                        ) {
                            Text(stringResource(R.string.general_import_from_arcaea))
                        }
                    }
                }
            }

            item { HorizontalDivider(Modifier.padding(vertical = 2.dp)) }

            item {
                OcrDependencyCrnnModelStatusViewer(crnnModelUiState)
            }
        }
    }
}
