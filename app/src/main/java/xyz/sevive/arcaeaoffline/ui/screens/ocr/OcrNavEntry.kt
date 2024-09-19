package xyz.sevive.arcaeaoffline.ui.screens.ocr

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Api
import androidx.compose.material.icons.filled.Queue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import xyz.sevive.arcaeaoffline.ui.AppViewModelProvider
import xyz.sevive.arcaeaoffline.ui.components.ocr.OcrDependencyCrnnModelStatusViewer
import xyz.sevive.arcaeaoffline.ui.components.ocr.OcrDependencyImageHashesDatabaseStatusViewer
import xyz.sevive.arcaeaoffline.ui.components.ocr.OcrDependencyKNearestModelStatusViewer
import xyz.sevive.arcaeaoffline.ui.navigation.MainScreenDestinations
import xyz.sevive.arcaeaoffline.ui.navigation.OcrScreenDestinations
import xyz.sevive.arcaeaoffline.ui.screens.NavEntryNavigateButton
import xyz.sevive.arcaeaoffline.ui.screens.ocr.dependencies.OcrDependenciesScreenViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrNavEntry(
    onNavigateToSubRoute: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val vm = viewModel<OcrDependenciesScreenViewModel>(factory = AppViewModelProvider.Factory)
    val kNearestModelUiState by vm.kNearestModelUiState.collectAsStateWithLifecycle()
    val imageHashesDatabaseUiState by vm.imageHashesDatabaseUiState.collectAsStateWithLifecycle()
    val crnnModelUiState by vm.crnnModelUiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier,
        topBar = {
            TopAppBar(title = { Text(stringResource(MainScreenDestinations.Ocr.title)) })
        },
        containerColor = Color.Transparent,
    ) {
        LazyColumn(Modifier.padding(it)) {
            item {
                Column {
                    OcrDependencyKNearestModelStatusViewer(kNearestModelUiState)
                    OcrDependencyImageHashesDatabaseStatusViewer(imageHashesDatabaseUiState)
                    OcrDependencyCrnnModelStatusViewer(crnnModelUiState)
                }
            }

            item {
                HorizontalDivider()
            }

            item {
                NavEntryNavigateButton(
                    titleResId = OcrScreenDestinations.Dependencies.title,
                    icon = Icons.Default.Api,
                ) {
                    onNavigateToSubRoute(OcrScreenDestinations.Dependencies.route)
                }
            }

            item {
                NavEntryNavigateButton(
                    titleResId = OcrScreenDestinations.Queue.title,
                    icon = Icons.Default.Queue,
                ) {
                    onNavigateToSubRoute(OcrScreenDestinations.Queue.route)
                }
            }
        }
    }
}
