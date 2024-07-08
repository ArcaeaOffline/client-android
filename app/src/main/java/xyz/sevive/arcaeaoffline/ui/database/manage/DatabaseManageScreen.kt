package xyz.sevive.arcaeaoffline.ui.database.manage

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.AppViewModelProvider
import xyz.sevive.arcaeaoffline.ui.SubScreenContainer
import xyz.sevive.arcaeaoffline.ui.components.IconRow


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatabaseManageScreen(
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DatabaseManageViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val actionRunning by viewModel.actionRunning.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()

    var showLogsSheet by rememberSaveable { mutableStateOf(false) }
    val logsSheetState = rememberModalBottomSheetState(confirmValueChange = { !actionRunning })

    LaunchedEffect(key1 = actionRunning) {
        if (actionRunning) showLogsSheet = true
    }

    LaunchedEffect(key1 = Unit) {
        viewModel.fuck()
    }

    if (showLogsSheet) {
        val headerIcon =
            if (actionRunning) Icons.Default.HourglassTop else Icons.AutoMirrored.Filled.Assignment
        val headerText = stringResource(
            if (actionRunning) R.string.general_please_wait else R.string.general_action_done
        )

        ModalBottomSheet(
            onDismissRequest = { showLogsSheet = false },
            sheetState = logsSheetState,
            dragHandle = {
                if (actionRunning) {
                    LinearProgressIndicator(Modifier.padding(vertical = 22.dp)) // drag handle default
                } else BottomSheetDefaults.DragHandle()
            },
            contentWindowInsets = {
                val padding = dimensionResource(R.dimen.page_padding)
                WindowInsets(left = padding, right = padding, bottom = padding * 2)
            },
        ) {
            Column(
                Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_padding)),
            ) {
                IconRow(
                    icon = { Icon(headerIcon, contentDescription = null, Modifier.size(36.dp)) },
                ) {
                    Text(headerText, style = MaterialTheme.typography.titleLarge)
                }

                Text(messages.joinToString("\n"), Modifier.animateContentSize())
            }
        }
    }

    SubScreenContainer(
        onNavigateUp = onNavigateUp,
        title = stringResource(R.string.database_manage_title),
    ) {
        LazyColumn(
            modifier,
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_padding)),
        ) {
            item {
                DatabaseManageImport(viewModel, Modifier.fillMaxWidth())
            }

            item {
                DatabaseManageExport(viewModel, Modifier.fillMaxWidth())
            }
        }
    }
}
