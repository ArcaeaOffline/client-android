package xyz.sevive.arcaeaoffline.ui.database

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.AppViewModelProvider
import xyz.sevive.arcaeaoffline.ui.components.TitleOutlinedCard

@Composable
fun DatabaseManageImport(viewModel: DatabaseManageViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val arcaeaInstalled = viewModel.isArcaeaInstalled(context)

    val importPacklistLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { fileUri ->
        if (fileUri != null) {
            val inputStream = context.contentResolver.openInputStream(fileUri)
            if (inputStream != null) {
                coroutineScope.launch { viewModel.importPacklist(inputStream) }
            }
        }
    }

    val importSonglistLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { fileUri ->
        if (fileUri != null) {
            val inputStream = context.contentResolver.openInputStream(fileUri)
            if (inputStream != null) {
                coroutineScope.launch { viewModel.importSonglist(inputStream) }
            }
        }
    }

    TitleOutlinedCard(title = { padding ->
        Row(
            Modifier.padding(padding),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.general_icon_text_padding)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.FileDownload, null)
            Text(stringResource(R.string.database_manage_import_title))
        }
    }, modifier = modifier.fillMaxWidth()) { padding ->
        Column(Modifier.padding(padding)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button({ importPacklistLauncher.launch("*/*") }) {
                    Text(stringResource(R.string.database_manage_import_packlist))
                }
                Button({ importSonglistLauncher.launch("*/*") }) {
                    Text(stringResource(R.string.database_manage_import_songlist))
                }
            }

            Button(
                {
                    coroutineScope.launch {
                        viewModel.importArcaeaApkFromInstalled(
                            context
                        )
                    }
                }, enabled = arcaeaInstalled
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.general_icon_text_padding)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (arcaeaInstalled) {
                        val arcaeaIcon = viewModel.getArcaeaIconFromInstalled(context)
                        val iconPx = LocalDensity.current.run {
                            24.dp.toPx()
                        }.toInt()
                        Image(
                            arcaeaIcon!!.toBitmap(iconPx, iconPx).asImageBitmap(), null
                        )
                        Text(stringResource(R.string.database_manage_import_from_arcaea_apk_installed))
                    } else {
                        Icon(Icons.Default.Cancel, null)
                        Text(stringResource(R.string.database_manage_import_from_arcaea_apk_installed_unavailable))
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatabaseManageScreen(
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DatabaseManageViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    Surface(modifier.fillMaxSize()) {
        Scaffold(topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.database_manage_title)) },
                navigationIcon = {
                    IconButton({ onNavigateUp() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
            )
        }) { padding ->
            LazyColumn(
                modifier
                    .padding(padding)
                    .padding(dimensionResource(R.dimen.general_page_padding)),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_arrangement_padding))
            ) {
                item {
                    DatabaseManageImport(viewModel)
                }
            }
        }
    }
}
