package xyz.sevive.arcaeaoffline.ui.activities.ocrfromshare

import android.content.Intent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import xyz.sevive.arcaeaoffline.MainActivity
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.helpers.needsSetup
import xyz.sevive.arcaeaoffline.ui.components.TextItem
import xyz.sevive.arcaeaoffline.ui.components.ocr.OcrDependencyCrnnModelStatusViewer
import xyz.sevive.arcaeaoffline.ui.components.ocr.OcrDependencyImageHashesDatabaseStatusViewer
import xyz.sevive.arcaeaoffline.ui.navigation.OcrSubScreen

@Composable
internal fun OcrFromShareOcrDependencyStatusCard(
    uiState: OcrFromShareViewModel.OcrDependencyViewersUiState,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Card(modifier) {
        OcrDependencyImageHashesDatabaseStatusViewer(uiState = uiState.imageHashesDatabase)
        OcrDependencyCrnnModelStatusViewer(uiState = uiState.crnnModel)

        if (uiState.imageHashesDatabase.statusDetail.needsSetup()) {
            TextItem(
                title = stringResource(R.string.ocr_guide_image_hashes_database_title),
                content = stringResource(R.string.ocr_guide_image_hashes_database_content),
                leadingIcon = Icons.Default.CloudDownload,
                trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                onClick = {
                    // User should expect returning to this activity after setting up,
                    // so do not finish activity here.
                    // CLEAR_TOP tells the running instance to recreate and re-read the
                    // route extra for jumping.
                    val intent =
                        Intent(context, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            putExtra(MainActivity.EXTRA_INITIAL_ROUTE, OcrSubScreen.Dependencies.route)
                        }
                    context.startActivity(intent)
                },
            )
        }
    }
}
