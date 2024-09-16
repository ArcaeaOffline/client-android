package xyz.sevive.arcaeaoffline.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.vectorResource
import xyz.sevive.arcaeaoffline.BuildConfig
import xyz.sevive.arcaeaoffline.R


@Composable
internal fun SettingsAboutCommunities(modifier: Modifier = Modifier) {
    val showRepo =
        remember { BuildConfig.COMMUNITY_REPO_URL != BuildConfig.COMMUNITY_VALUE_PLACEHOLDER }
    val showIssues =
        remember { BuildConfig.COMMUNITY_ISSUES_URL != BuildConfig.COMMUNITY_VALUE_PLACEHOLDER }
    val showDiscord =
        remember { BuildConfig.COMMUNITY_DISCORD_URL != BuildConfig.COMMUNITY_VALUE_PLACEHOLDER }
    val showQq =
        remember { BuildConfig.COMMUNITY_QQ_URL != BuildConfig.COMMUNITY_VALUE_PLACEHOLDER }

    val uriHandler = LocalUriHandler.current

    Column(modifier) {
        Row {
            CompositionLocalProvider(
                LocalContentColor provides MaterialTheme.colorScheme.primary
            ) {
                if (showRepo) {
                    IconButton(onClick = { uriHandler.openUri(BuildConfig.COMMUNITY_REPO_URL) }) {
                        Icon(Icons.Default.Code, contentDescription = "Source code")
                    }
                }

                if (showIssues) {
                    IconButton(onClick = { uriHandler.openUri(BuildConfig.COMMUNITY_ISSUES_URL) }) {
                        Icon(Icons.Default.Feedback, contentDescription = "Issues")
                    }
                }
            }

            CompositionLocalProvider(
                LocalContentColor provides MaterialTheme.colorScheme.secondary
            ) {
                if (showDiscord) {
                    IconButton(onClick = { uriHandler.openUri(BuildConfig.COMMUNITY_DISCORD_URL) }) {
                        Icon(
                            ImageVector.vectorResource(R.drawable.simple_icons_discord),
                            contentDescription = "Discord",
                        )
                    }
                }

                if (showQq) {
                    IconButton(onClick = { uriHandler.openUri(BuildConfig.COMMUNITY_QQ_URL) }) {
                        Icon(
                            ImageVector.vectorResource(R.drawable.simple_icons_tencentqq),
                            contentDescription = "QQ",
                        )
                    }
                }
            }
        }
    }
}
