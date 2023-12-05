package xyz.sevive.arcaeaoffline.ui.database.manage

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.components.IconRow
import xyz.sevive.arcaeaoffline.ui.components.TitleOutlinedCard
import java.util.zip.ZipInputStream


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
                coroutineScope.launch { viewModel.importPacklist(inputStream, context) }
            }
        }
    }

    val importSonglistLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { fileUri ->
        if (fileUri != null) {
            val inputStream = context.contentResolver.openInputStream(fileUri)
            if (inputStream != null) {
                coroutineScope.launch { viewModel.importSonglist(inputStream, context) }
            }
        }
    }

    val importArcaeaApkLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { fileUri ->
        if (fileUri != null) {
            val inputStream = context.contentResolver.openInputStream(fileUri)
            if (inputStream != null) {
                Toast.makeText(
                    context,
                    R.string.database_manage_import_from_arcaea_apk_please_wait,
                    Toast.LENGTH_LONG
                ).show()
                coroutineScope.launch {
                    withContext(Dispatchers.IO) {
                        ZipInputStream(inputStream).use {
                            viewModel.importArcaeaApkFromSelect(it, context)
                        }
                    }
                }
            }
        }
    }

    TitleOutlinedCard(title = { padding ->
        IconRow(
            modifier = modifier.padding(padding),
            icon = { Icon(Icons.Default.FileDownload, null) }) {
            Text(stringResource(R.string.database_manage_import_title))
        }
    }, modifier = modifier) { padding ->
        Column(Modifier.padding(padding)) {
            Button({ importPacklistLauncher.launch("*/*") }) {
                IconRow(icon = { Icon(Icons.Default.FileOpen, null) }) {
                    Text(stringResource(R.string.database_manage_import_packlist))
                }
            }
            Button({ importSonglistLauncher.launch("*/*") }) {
                IconRow(icon = { Icon(Icons.Default.FileOpen, null) }) {
                    Text(stringResource(R.string.database_manage_import_songlist))
                }
            }

            Button({ importArcaeaApkLauncher.launch("*/*") }) {
                IconRow(icon = { Icon(Icons.Default.FileOpen, null) }) {
                    Text(stringResource(R.string.database_manage_import_from_arcaea_apk))
                }
            }

            Button(
                { coroutineScope.launch { viewModel.importArcaeaApkFromInstalled(context) } },
                enabled = arcaeaInstalled
            ) {
                IconRow(icon = {
                    if (arcaeaInstalled) {
                        val arcaeaIcon = viewModel.getArcaeaIconFromInstalled(context)
                        val iconPx = LocalDensity.current.run { 24.dp.toPx() }.toInt()
                        Image(arcaeaIcon!!.toBitmap(iconPx, iconPx).asImageBitmap(), null)
                    } else {
                        Icon(Icons.Default.Cancel, null)
                    }
                }) {
                    if (arcaeaInstalled) {
                        Text(stringResource(R.string.database_manage_import_from_arcaea_apk_installed))
                    } else {
                        Text(stringResource(R.string.database_manage_import_from_arcaea_apk_installed_unavailable))
                    }
                }
            }
        }
    }
}
