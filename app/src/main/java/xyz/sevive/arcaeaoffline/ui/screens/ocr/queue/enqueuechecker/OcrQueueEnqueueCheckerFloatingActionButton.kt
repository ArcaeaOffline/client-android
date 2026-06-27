package xyz.sevive.arcaeaoffline.ui.screens.ocr.queue.enqueuechecker

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Badge
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme

@Composable
internal fun OcrQueueEnqueueCheckerFloatingActionButton(
    onClick: () -> Unit,
    isVisible: Boolean,
    badgeCount: Int?,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = isVisible,
        modifier = modifier,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Box {
            FloatingActionButton(onClick = onClick) {
                Icon(
                    ImageVector.vectorResource(R.drawable.ic_image_plus),
                    contentDescription = null,
                )
            }

            if (badgeCount != null) {
                Badge(
                    modifier =
                        Modifier
                            .align(Alignment.TopStart)
                            .offset(x = (-4).dp, y = (-4).dp),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                ) {
                    Text(badgeCount.toString())
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun OcrQueueEnqueueCheckerFloatingActionButtonPreview() {
    ArcaeaOfflineTheme {
        Surface {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OcrQueueEnqueueCheckerFloatingActionButton(
                    onClick = {},
                    isVisible = true,
                    badgeCount = null,
                )

                OcrQueueEnqueueCheckerFloatingActionButton(
                    onClick = {},
                    isVisible = true,
                    badgeCount = 5,
                )
            }
        }
    }
}
