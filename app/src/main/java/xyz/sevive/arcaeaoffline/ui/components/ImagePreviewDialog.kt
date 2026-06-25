package xyz.sevive.arcaeaoffline.ui.components

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.github.panpf.zoomimage.SketchZoomAsyncImage
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.helpers.context.getFilename

@Composable
private fun TopFileInfoCard(
    uri: Uri,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val filename = remember(uri.toString()) { context.getFilename(uri) ?: "-" }
    var toggle by rememberSaveable { mutableStateOf(true) }

    Card(
        onClick = { toggle = !toggle },
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(0.5f)),
    ) {
        Text(
            if (toggle) filename else uri.toString(),
            Modifier.padding(dimensionResource(R.dimen.page_padding)),
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImagePreviewDialogFullscreen(
    onDismissRequest: () -> Unit,
    uri: Uri,
    modifier: Modifier = Modifier,
    properties: DialogProperties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
) {
    Dialog(onDismissRequest, properties = properties) {
        Scaffold(
            modifier,
            contentWindowInsets = WindowInsets.safeContent,
            containerColor = MaterialTheme.colorScheme.background.copy(0.3f),
            topBar = {
                TopFileInfoCard(
                    uri,
                    Modifier
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .fillMaxWidth(),
                )
            },
            bottomBar = {
                Box(
                    Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(bottom = 28.dp)
                        .fillMaxWidth(),
                ) {
                    FilledTonalIconButton(
                        onClick = onDismissRequest,
                        Modifier
                            .align(Alignment.Center)
                            .size(56.dp),
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, Modifier.size(28.dp))
                    }
                }
            },
        ) { innerPadding ->
            SketchZoomAsyncImage(
                uri = uri.toString(),
                contentDescription = null,
                modifier =
                    Modifier
                        .consumeWindowInsets(innerPadding)
                        .fillMaxSize(),
            )
        }
    }
}
