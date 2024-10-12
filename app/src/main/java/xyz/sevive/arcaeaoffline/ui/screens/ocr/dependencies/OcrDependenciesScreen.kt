package xyz.sevive.arcaeaoffline.ui.screens.ocr.dependencies

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.helpers.ArcaeaResourcesStateHolder
import xyz.sevive.arcaeaoffline.helpers.rememberFileChooserLauncher
import xyz.sevive.arcaeaoffline.ui.AppViewModelProvider
import xyz.sevive.arcaeaoffline.ui.SubScreenContainer
import xyz.sevive.arcaeaoffline.ui.components.ArcaeaAppIcon
import xyz.sevive.arcaeaoffline.ui.components.ocr.OcrDependencyCrnnModelStatusViewer
import xyz.sevive.arcaeaoffline.ui.components.ocr.OcrDependencyImageHashesDatabaseStatusViewer
import xyz.sevive.arcaeaoffline.ui.components.ocr.OcrDependencyKNearestModelStatusViewer
import xyz.sevive.arcaeaoffline.ui.components.preferences.TextPreferencesWidget
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

    val canBuildHashesDatabase by ArcaeaResourcesStateHolder.canBuildHashesDatabase.collectAsStateWithLifecycle()
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
                OcrDependencyKNearestModelStatusViewer(kNearestModelUiState)
            }

            item {
                TextPreferencesWidget(
                    onClick = { kNearestModelFileChooserLauncher.launch("*/*") },
                    title = stringResource(R.string.general_import),
                    leadingIcon = Icons.Default.FileOpen,
                )
            }

            item { HorizontalDivider() }

            item {
                OcrDependencyImageHashesDatabaseStatusViewer(imageHashesDatabaseUiState)
            }

            item {
                TextPreferencesWidget(
                    onClick = { imageHashesDatabaseFileChooserLauncher.launch("*/*") },
                    leadingIcon = Icons.Default.FileOpen,
                    title = stringResource(R.string.general_import),
                )
            }

            item {
                val stringId = if (canBuildHashesDatabase) R.string.general_import_from_arcaea
                else R.string.arcaea_button_resource_unavailable

                TextPreferencesWidget(
                    enabled = buildHashesDatabaseButtonEnabled,
                    onClick = { viewModel.requestImageHashesDatabaseBuild() },
                    leadingSlot = { ArcaeaAppIcon(forceDisabled = !canBuildHashesDatabase) },
                    title = stringResource(stringId),
                )
            }

            item { HorizontalDivider() }

            item {
                OcrDependencyCrnnModelStatusViewer(crnnModelUiState)
            }
        }
    }
}
