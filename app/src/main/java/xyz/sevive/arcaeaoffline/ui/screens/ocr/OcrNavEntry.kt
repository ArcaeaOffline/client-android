package xyz.sevive.arcaeaoffline.ui.screens.ocr

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
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
import org.koin.compose.viewmodel.koinViewModel
import xyz.sevive.arcaeaoffline.ui.components.ocr.OcrDependencyCrnnModelStatusViewer
import xyz.sevive.arcaeaoffline.ui.components.ocr.OcrDependencyImageHashesDatabaseStatusViewer
import xyz.sevive.arcaeaoffline.ui.components.ocr.OcrDependencyKNearestModelStatusViewer
import xyz.sevive.arcaeaoffline.ui.navigation.LocalListDetailNavigationContext
import xyz.sevive.arcaeaoffline.ui.navigation.MainScreen
import xyz.sevive.arcaeaoffline.ui.navigation.OcrSubScreen
import xyz.sevive.arcaeaoffline.ui.screens.NavEntryNavigateButton
import xyz.sevive.arcaeaoffline.ui.screens.ocr.dependencies.OcrDependenciesScreenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrNavEntry(modifier: Modifier = Modifier) {
    val navContext = LocalListDetailNavigationContext.current
    val vm = koinViewModel<OcrDependenciesScreenViewModel>()
    val kNearestModelUiState by vm.kNearestModelUiState.collectAsStateWithLifecycle()
    val imageHashesDatabaseUiState by vm.imageHashesDatabaseUiState.collectAsStateWithLifecycle()
    val crnnModelUiState by vm.crnnModelUiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier,
        topBar = {
            TopAppBar(title = { Text(stringResource(MainScreen.Ocr.title)) })
        },
        containerColor = Color.Transparent,
    ) {
        LazyColumn(
            Modifier
                .fillMaxSize()
                .consumeWindowInsets(it),
            contentPadding = it,
        ) {
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
                    titleResId = OcrSubScreen.Dependencies.title,
                    icon = Icons.Default.Api,
                ) {
                    navContext.navigateToDetail(OcrSubScreen.Dependencies.route)
                }
            }

            item {
                NavEntryNavigateButton(
                    titleResId = OcrSubScreen.Queue.title,
                    icon = Icons.Default.Queue,
                ) {
                    navContext.navigateToDetail(OcrSubScreen.Queue.route)
                }
            }
        }
    }
}
