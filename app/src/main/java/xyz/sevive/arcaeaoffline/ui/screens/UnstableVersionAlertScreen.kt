package xyz.sevive.arcaeaoffline.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.HorizontalRule
import com.halilibo.richtext.ui.material3.Material3RichText
import kotlinx.coroutines.delay
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.components.IconRow
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme

@Composable
private fun TitleContent(textStyle: TextStyle, onConfirm: () -> Unit) {
    val iconDp = with(LocalDensity.current) { (textStyle.fontSize.value * 2.5).sp.toDp() }
    val interactionSource = remember { MutableInteractionSource() }

    var clickCount by rememberSaveable { mutableIntStateOf(0) }
    LaunchedEffect(clickCount) { if (clickCount >= 5) onConfirm() }

    Icon(
        painterResource(R.drawable.ic_unstable_build),
        null,
        Modifier
            .size(iconDp)
            .clickable(interactionSource = interactionSource, indication = null) {
                clickCount += 1
            },
    )
    Text(
        stringResource(R.string.unstable_version_alert_title),
        fontWeight = FontWeight.Bold,
        style = textStyle,
    )
}

@Composable
private fun DetailsContentContainer(
    modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit
) {
    OutlinedCard(
        modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(0.5f),
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        ),
        content = content,
    )
}

@Composable
private fun DetailsContent(modifier: Modifier = Modifier) {
    Material3RichText(modifier) {
        Markdown(stringResource(R.string.unstable_version_alert_screen_details_markdown))
    }
}

@Composable
private fun ConfirmPromptContent(modifier: Modifier = Modifier) {
    Material3RichText(modifier) {
        Markdown(stringResource(R.string.unstable_version_alert_screen_confirm_prompt_markdown))
    }
}

@Composable
private fun ActionsContent(
    countdownValue: Int,
    onConfirm: () -> Unit,
    onDeny: () -> Unit,
) {
    val enabled = remember(countdownValue) { countdownValue <= 0 }
    val progressIndicatorValue by animateFloatAsState(
        targetValue = (countdownValue - 1) / 10f,
        animationSpec = tween(1000, easing = LinearEasing),
        label = "progressIndicatorValue",
    )

    OutlinedButton(
        onClick = { onDeny() },
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.error,
        ),
    ) {
        IconRow {
            Icon(Icons.AutoMirrored.Default.Logout, null)
            Text(stringResource(R.string.unstable_version_alert_screen_actions_deny))
        }
    }

    Button(
        onClick = { onConfirm() },
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError,
        ),
        enabled = enabled,
    ) {
        IconRow {
            when {
                enabled -> Icon(Icons.AutoMirrored.Default.Login, null)
                else -> CircularProgressIndicator(
                    progress = { progressIndicatorValue },
                    Modifier
                        .size(22.dp)
                        .padding(1.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onSurface,
                    trackColor = Color.Transparent,
                )
            }

            Text(stringResource(R.string.unstable_version_alert_screen_actions_confirm))
        }
    }
}

@Composable
fun UnstableVersionAlertScreenExpanded(
    countdownValue: Int,
    onConfirm: () -> Unit,
    onDeny: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(contentColor = MaterialTheme.colorScheme.error) {
        Row(modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.weight(0.5f))

            Column(
                Modifier.weight(3f),
                Arrangement.spacedBy(dimensionResource(R.dimen.list_padding)),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_padding)),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    TitleContent(
                        textStyle = MaterialTheme.typography.displayLarge,
                        onConfirm = onConfirm,
                    )
                }
                DetailsContentContainer(Modifier.height(400.dp)) {
                    LazyColumn(
                        contentPadding = PaddingValues(dimensionResource(R.dimen.page_padding))
                    ) {
                        item {
                            DetailsContent()
                        }
                    }
                }
            }

            Spacer(Modifier.weight(0.25f))

            Column(
                Modifier.weight(2f),
                Arrangement.spacedBy(dimensionResource(R.dimen.list_padding)),
            ) {
                ConfirmPromptContent()
                Column {
                    ActionsContent(countdownValue, onConfirm = onConfirm, onDeny = onDeny)
                }
            }

            Spacer(Modifier.weight(0.5f))
        }
    }
}

@Composable
fun UnstableVersionAlertScreenDefault(
    countdownValue: Int,
    onConfirm: () -> Unit,
    onDeny: () -> Unit,
    windowSizeClass: WindowSizeClass,
) {
    val pagePadding = dimensionResource(R.dimen.page_padding)

    val confirmPromptInDetailsContainer =
        windowSizeClass.heightSizeClass < WindowHeightSizeClass.Expanded

    Scaffold(
        contentColor = MaterialTheme.colorScheme.error,
        topBar = {
            Row(
                Modifier.padding(pagePadding),
                Arrangement.spacedBy(dimensionResource(R.dimen.list_padding)),
                Alignment.Bottom,
            ) {
                TitleContent(
                    textStyle = if (windowSizeClass.widthSizeClass <= WindowWidthSizeClass.Compact) {
                        MaterialTheme.typography.displaySmall
                    } else {
                        MaterialTheme.typography.displayMedium
                    },
                    onConfirm = onConfirm,
                )
            }
        },
        bottomBar = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(pagePadding),
                horizontalAlignment = Alignment.End,
            ) {
                if (!confirmPromptInDetailsContainer) {
                    ConfirmPromptContent()
                }
                ActionsContent(countdownValue, onConfirm = onConfirm, onDeny = onDeny)
            }
        },
    ) {
        DetailsContentContainer(
            Modifier
                .padding(it)
                .padding(pagePadding)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(dimensionResource(R.dimen.page_padding))
            ) {
                if (confirmPromptInDetailsContainer) {
                    item {
                        ConfirmPromptContent()
                    }

                    item {
                        Material3RichText {
                            HorizontalRule()
                        }
                    }
                }

                item {
                    DetailsContent()
                }
            }
        }
    }
}

/**
 * a 10s countdown to ensure user has skim through the notice
 * an extra 1 second to let user know what's going on:
 * this app is wasting my precious 10 seconds of time,
 * forcing me to confirm a fucking long and boring alert
 */
@Composable
fun rememberContentReadCountdown(): Int {
    var countdown by rememberSaveable { mutableIntStateOf(11) }
    LaunchedEffect(key1 = countdown) {
        while (countdown > 0) {
            delay(1000L)
            countdown -= 1
        }
    }
    return countdown
}

@Composable
fun UnstableVersionAlertScreen(
    onConfirm: () -> Unit,
    onDeny: () -> Unit,
    windowSizeClass: WindowSizeClass,
) {
    val countdown = rememberContentReadCountdown()

    if (windowSizeClass.widthSizeClass >= WindowWidthSizeClass.Expanded) {
        UnstableVersionAlertScreenExpanded(countdown, onConfirm, onDeny)
    } else {
        UnstableVersionAlertScreenDefault(countdown, onConfirm, onDeny, windowSizeClass)
    }
}

@Preview(showBackground = true, showSystemUi = true, device = Devices.DESKTOP)
@Composable
private fun UnstableVersionAlertScreenExpandedPreview() {
    val countdown = rememberContentReadCountdown()

    ArcaeaOfflineTheme {
        UnstableVersionAlertScreenExpanded(countdown, {}, {})
    }
}
