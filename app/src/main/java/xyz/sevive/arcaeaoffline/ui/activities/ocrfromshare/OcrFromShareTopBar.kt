package xyz.sevive.arcaeaoffline.ui.activities.ocrfromshare

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import xyz.sevive.arcaeaoffline.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun OcrFromShareTopBar() {
    TopAppBar(
        title = { Text(text = stringResource(R.string.title_activity_ocr_from_share)) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
    )
}
