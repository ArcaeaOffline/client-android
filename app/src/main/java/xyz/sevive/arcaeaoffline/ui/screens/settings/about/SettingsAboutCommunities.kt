package xyz.sevive.arcaeaoffline.ui.screens.settings.about

import android.content.ClipData
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.shared.BuildKonfig
import xyz.sevive.arcaeaoffline.ui.components.BasicAlertDialogSurface
import xyz.sevive.arcaeaoffline.ui.theme.spacing
import kotlin.time.Duration.Companion.seconds

private data class CommunityEntry(
    val title: String,
    val link: String,
    val icon: @Composable () -> Painter,
) {
    val isVisible = link != BuildKonfig.COMMUNITY_VALUE_PLACEHOLDER
}

private val COMMUNITY_ENTRIES =
    listOf(
        CommunityEntry("GitHub", BuildKonfig.COMMUNITY_REPO_URL, { painterResource(R.drawable.simple_icons_github) }),
        CommunityEntry("Issues", BuildKonfig.COMMUNITY_ISSUES_URL, { rememberVectorPainter(Icons.Filled.Feedback) }),
        CommunityEntry("Discord", BuildKonfig.COMMUNITY_DISCORD_URL, { painterResource(R.drawable.simple_icons_discord) }),
        CommunityEntry("QQ", BuildKonfig.COMMUNITY_QQ_URL, { painterResource(R.drawable.simple_icons_tencentqq) }),
    ).filter { it.isVisible }

@Composable
private fun CopyLinkIconButton(entry: CommunityEntry) {
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()

    val (title, link) = entry

    var isCopied by remember { mutableStateOf(false) }
    var copyCount by remember { mutableIntStateOf(0) }
    LaunchedEffect(copyCount) {
        if (copyCount > 0) {
            isCopied = true
            delay(1.seconds)
            isCopied = false
        }
    }

    IconButton(
        onClick = {
            coroutineScope.launch {
                val clipData = ClipData.newPlainText("Community link ($title)", link)
                clipboard.setClipEntry(clipData.toClipEntry())
                copyCount++
            }
        },
    ) {
        AnimatedContent(isCopied) {
            if (it) {
                Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            } else {
                Icon(Icons.Default.ContentCopy, contentDescription = null)
            }
        }
    }
}

@Composable
internal fun SettingsAboutCommunities(modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current

    val accentColor = MaterialTheme.colorScheme.secondary

    var selectedEntryTitle by rememberSaveable { mutableStateOf<String?>(null) }

    selectedEntryTitle?.let { selectedTitle ->
        val entry = COMMUNITY_ENTRIES.firstOrNull { it.title == selectedTitle } ?: return@let
        val (title, link, icon) = entry

        BasicAlertDialogSurface(
            onDismissRequest = { selectedEntryTitle = null },
        ) { paddingValues ->
            Column(
                Modifier.padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CompositionLocalProvider(
                        LocalContentColor provides accentColor,
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(icon(), contentDescription = null)
                            Text(title, fontWeight = FontWeight.Bold)
                        }
                    }

                    FilledIconButton(onClick = { uriHandler.openUri(link) }) {
                        Icon(Icons.AutoMirrored.Default.OpenInNew, contentDescription = null)
                    }
                }

                OutlinedTextField(
                    value = link,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { CopyLinkIconButton(entry) },
                )
            }
        }
    }

    Row(
        modifier,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompositionLocalProvider(
            LocalContentColor provides accentColor,
        ) {
            COMMUNITY_ENTRIES.forEach {
                IconButton(onClick = { selectedEntryTitle = it.title }) {
                    Icon(it.icon(), contentDescription = "Community link (${it.title})")
                }
            }
        }
    }
}
