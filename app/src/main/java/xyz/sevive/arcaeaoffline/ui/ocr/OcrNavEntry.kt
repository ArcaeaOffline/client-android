package xyz.sevive.arcaeaoffline.ui.ocr

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Queue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.components.ActionButton
import xyz.sevive.arcaeaoffline.ui.components.ocr.OcrDependencyKnnModelStatus
import xyz.sevive.arcaeaoffline.ui.components.ocr.OcrDependencyPhashDatabaseStatus
import xyz.sevive.arcaeaoffline.ui.models.OcrDependencyViewModel
import xyz.sevive.arcaeaoffline.ui.navigation.OcrScreen


@Composable
fun OcrNavEntry(
    onNavigateToSubRoute: (String) -> Unit,
    modifier: Modifier = Modifier,
    ocrDependencyViewModel: OcrDependencyViewModel = viewModel()
) {
    val context = LocalContext.current

    ocrDependencyViewModel.reload(context)

    val knnModelStatus by ocrDependencyViewModel.knnModelState.collectAsState()
    val phashDatabaseState by ocrDependencyViewModel.phashDatabaseState.collectAsState()

    Scaffold(
        modifier,
        topBar = {
            Text(
                stringResource(R.string.nav_ocr),
                Modifier.padding(0.dp, dimensionResource(R.dimen.general_page_padding)),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        containerColor = Color.Transparent,
    ) {
        LazyColumn(
            Modifier.padding(it),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.main_screen_list_arrangement_spaced_by)),
        ) {
            item {
                Column {
                    OcrDependencyKnnModelStatus(knnModelStatus)
                    OcrDependencyPhashDatabaseStatus(phashDatabaseState)
                }
            }

            item {
                ActionButton(
                    onClick = { onNavigateToSubRoute(OcrScreen.Queue.route) },
                    title = stringResource(OcrScreen.Queue.title),
                    headSlot = {
                        Icon(Icons.Default.Queue, null)
                    },
                    tailSlot = {
                        Icon(Icons.AutoMirrored.Default.ArrowForward, null)
                    },
                )
            }
        }
    }
}
