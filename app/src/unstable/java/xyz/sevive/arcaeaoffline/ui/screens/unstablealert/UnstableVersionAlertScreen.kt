package xyz.sevive.arcaeaoffline.ui.screens.unstablealert

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
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
internal fun TitleContent(titleTextStyle: TextStyle) {
    val iconDp = with(LocalDensity.current) { (titleTextStyle.fontSize.value * 2.5).sp.toDp() }

    Icon(
        painterResource(R.drawable.ic_unstable_build),
        null,
        Modifier.size(iconDp),
    )
    Text(
        stringResource(R.string.unstable_version_alert_title),
        fontWeight = FontWeight.Bold,
        style = titleTextStyle,
    )
}

@Composable
internal fun DetailsContentContainer(
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
internal fun DetailsContent(modifier: Modifier = Modifier) {
    Material3RichText(modifier) {
        Markdown(stringResource(R.string.unstable_version_alert_screen_details_markdown))
    }
}

@Composable
internal fun ConfirmPromptContent(modifier: Modifier = Modifier) {
    Material3RichText(modifier) {
        Markdown(stringResource(R.string.unstable_version_alert_screen_confirm_prompt_markdown))
    }
}

@Composable
internal fun ActionsContent(
    onConfirm: () -> Unit,
    onDeny: () -> Unit,
) {
    // a 10s countdown to ensure user has skim through the notice
    // an extra 1 second to let user know what's going on:
    // this app is wasting my precious 10 seconds of time,
    // forcing me to confirm a fxxking long and boring alert
    var confirmContentReadCountdown by remember { mutableIntStateOf(11) }
    LaunchedEffect(key1 = confirmContentReadCountdown) {
        while (confirmContentReadCountdown > 0) {
            delay(1000L)
            confirmContentReadCountdown -= 1
        }
    }

    OutlinedButton(
        onClick = { onDeny() },
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.error,
        ),
    ) {
        IconRow(icon = { Icon(Icons.Default.Logout, null) }) {
            Text(stringResource(R.string.unstable_version_alert_screen_actions_deny))
        }
    }

    Button(
        onClick = { onConfirm() },
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError,
        ),
        enabled = confirmContentReadCountdown <= 0,
    ) {
        IconRow(icon = {
            if (confirmContentReadCountdown <= 0) {
                Icon(Icons.Default.Login, null)
            } else {
                Text(
                    "(${confirmContentReadCountdown})",
                    fontSize = with(LocalDensity.current) { 20.dp.toSp() },
                )
            }
        }) {
            Text(stringResource(R.string.unstable_version_alert_screen_actions_confirm))
        }
    }
}

@Composable
fun UnstableVersionAlertScreenExpanded(
    onConfirm: () -> Unit,
    onDeny: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(contentColor = MaterialTheme.colorScheme.error) {
        Row(modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.weight(0.5f))

            Column(
                Modifier.weight(3f),
                Arrangement.spacedBy(dimensionResource(R.dimen.list_arrangement_padding)),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_arrangement_padding)),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    TitleContent(titleTextStyle = MaterialTheme.typography.displayLarge)
                }
                DetailsContentContainer(Modifier.height(400.dp)) {
                    LazyColumn(
                        contentPadding = PaddingValues(dimensionResource(R.dimen.general_page_padding))
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
                Arrangement.spacedBy(dimensionResource(R.dimen.list_arrangement_padding)),
            ) {
                ConfirmPromptContent()
                Column {
                    ActionsContent(onConfirm = onConfirm, onDeny = onDeny)
                }
            }

            Spacer(Modifier.weight(0.5f))
        }
    }
}

@Composable
fun UnstableVersionAlertScreenDefault(
    onConfirm: () -> Unit,
    onDeny: () -> Unit,
    windowSizeClass: WindowSizeClass,
) {
    val pagePadding = dimensionResource(R.dimen.general_page_padding)

    val confirmPromptInDetailsContainer =
        windowSizeClass.heightSizeClass < WindowHeightSizeClass.Expanded

    Scaffold(
        contentColor = MaterialTheme.colorScheme.error,
        topBar = {
            Row(
                Modifier.padding(pagePadding),
                Arrangement.spacedBy(dimensionResource(R.dimen.list_arrangement_padding)),
                Alignment.Bottom,
            ) {
                TitleContent(
                    titleTextStyle = if (windowSizeClass.widthSizeClass <= WindowWidthSizeClass.Compact) {
                        MaterialTheme.typography.displaySmall
                    } else {
                        MaterialTheme.typography.displayMedium
                    }
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
                ActionsContent(onConfirm = onConfirm, onDeny = onDeny)
            }
        },
    ) {
        DetailsContentContainer(
            Modifier
                .padding(it)
                .padding(pagePadding)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(dimensionResource(R.dimen.general_page_padding))
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

@Composable
fun UnstableVersionAlertScreen(
    onConfirm: () -> Unit,
    onDeny: () -> Unit,
    windowSizeClass: WindowSizeClass,
) {
    if (windowSizeClass.widthSizeClass >= WindowWidthSizeClass.Expanded) {
        UnstableVersionAlertScreenExpanded(onConfirm, onDeny)
    } else {
        UnstableVersionAlertScreenDefault(onConfirm, onDeny, windowSizeClass)
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    device = Devices.DESKTOP,
)
@Composable
fun UnstableVersionAlertScreenExpandedPreview() {
    ArcaeaOfflineTheme {
        UnstableVersionAlertScreenExpanded({}, {})
    }
}
