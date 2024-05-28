package xyz.sevive.arcaeaoffline.ui.common.imagepreview

import android.graphics.BitmapFactory
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.origeek.imageViewer.viewer.rememberViewerState
import xyz.sevive.arcaeaoffline.R
import java.io.InputStream

@Composable
fun ImagePreviewDialog(
    imageBitmap: ImageBitmap,
    onDismiss: () -> Unit,
    topBarContent: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val imageViewerState = rememberViewerState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Column(Modifier.fillMaxSize()) {
            if (topBarContent != null) {
                Surface(
                    Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                ) {
                    Column(Modifier.padding(dimensionResource(R.dimen.card_padding))) {
                        topBarContent()
                    }
                }
            }

            // `com.origeek.imageViewer` minSdk 24
            if (Build.VERSION.SDK_INT >= 24) {
                ImagePreviewDialogImage(
                    imageBitmap = imageBitmap,
                    imageViewerState = imageViewerState,
                    onDismiss = onDismiss,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                ImagePreviewDialogImageCompat(
                    imageBitmap = imageBitmap,
                    onDismiss = onDismiss,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun ImagePreviewDialog(
    inputStream: InputStream?,
    onDismiss: () -> Unit,
    topBarContent: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val context = LocalContext.current

    if (inputStream == null) {
        Toast.makeText(context, "Cannot preview image", Toast.LENGTH_LONG).show()
        onDismiss()
        return
    }
    val bitmap = inputStream.use { BitmapFactory.decodeStream(inputStream) }

    ImagePreviewDialog(
        imageBitmap = bitmap.asImageBitmap(),
        onDismiss = onDismiss,
        topBarContent = topBarContent,
    )
}
