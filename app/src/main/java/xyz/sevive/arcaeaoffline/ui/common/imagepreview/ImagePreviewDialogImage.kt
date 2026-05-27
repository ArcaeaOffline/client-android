package xyz.sevive.arcaeaoffline.ui.common.imagepreview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import com.jvziyaoyao.scale.image.viewer.ImageViewer
import com.jvziyaoyao.scale.zoomable.zoomable.ZoomableGestureScope
import com.jvziyaoyao.scale.zoomable.zoomable.ZoomableViewState
import kotlinx.coroutines.launch

@Composable
fun ImagePreviewDialogImage(
    imageBitmap: ImageBitmap,
    zoomableState: ZoomableViewState,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()

    ImageViewer(
        state = zoomableState,
        model = imageBitmap,
        modifier = modifier,
        detectGesture =
            ZoomableGestureScope(
                onTap = { onDismiss() },
                onDoubleTap = { coroutineScope.launch { zoomableState.toggleScale(it) } },
            ),
    )
}
