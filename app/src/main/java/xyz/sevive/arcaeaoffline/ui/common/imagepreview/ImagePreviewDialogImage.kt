package xyz.sevive.arcaeaoffline.ui.common.imagepreview

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import com.origeek.imageViewer.viewer.ImageViewer
import com.origeek.imageViewer.viewer.ImageViewerState
import kotlinx.coroutines.launch

@Composable
fun ImagePreviewDialogImage(
    imageBitmap: ImageBitmap,
    imageViewerState: ImageViewerState,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()

    ImageViewer(
        state = imageViewerState,
        model = imageBitmap,
        modifier = modifier,
        detectGesture = {
            onTap = { onDismiss() }
            onDoubleTap = { coroutineScope.launch { imageViewerState.toggleScale(it) } }
        },
    )
}

@Composable
fun ImagePreviewDialogImageCompat(
    imageBitmap: ImageBitmap,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Image(
        imageBitmap,
        contentDescription = "Preview image",
        modifier.clickable { onDismiss() },
    )
}
