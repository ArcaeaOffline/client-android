package xyz.sevive.arcaeaoffline.helpers

import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha


const val DISABLED_ALPHA = .38f
const val SECONDARY_ALPHA = .75f

@Composable
fun rememberFileChooserLauncher(
    onResult: (Uri?) -> Unit,
): ManagedActivityResultLauncher<String, Uri?> {
    return rememberLauncherForActivityResult(ActivityResultContracts.GetContent(), onResult)
}

fun Modifier.disabledItemAlpha() = this.alpha(DISABLED_ALPHA)
fun Modifier.secondaryItemAlpha() = this.alpha(SECONDARY_ALPHA)
