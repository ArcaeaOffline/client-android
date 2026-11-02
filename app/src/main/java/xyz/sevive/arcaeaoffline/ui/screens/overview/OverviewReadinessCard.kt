package xyz.sevive.arcaeaoffline.ui.screens.overview

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.database.repositories.DatabaseReadiness
import xyz.sevive.arcaeaoffline.helpers.formatAsLocalizedDateTime
import xyz.sevive.arcaeaoffline.ui.components.TextItem
import xyz.sevive.arcaeaoffline.ui.theme.spacing

@Composable
private fun SetupStepItem(
    title: String,
    done: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TextItem(
        title = title,
        modifier = modifier,
        leadingIcon = if (done) Icons.Default.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
        leadingIconTint = if (done) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
        onClick = onClick,
    )
}

@Composable
internal fun OverviewReadinessCard(
    uiState: OverviewViewModel.ReadinessUiState,
    onNavigateToResources: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val summary = uiState.summary

    Card(modifier) {
        when (summary.readiness) {
            DatabaseReadiness.EMPTY, DatabaseReadiness.SONGS_ONLY -> {
                Text(
                    stringResource(R.string.home_setup_header),
                    style = MaterialTheme.typography.titleMedium,
                    modifier =
                        Modifier.padding(
                            start = MaterialTheme.spacing.pagePadding,
                            end = MaterialTheme.spacing.pagePadding,
                            top = MaterialTheme.spacing.cardPadding,
                        ),
                )

                SetupStepItem(
                    title = stringResource(R.string.home_setup_step_songs),
                    done = summary.songCount > 0,
                    onClick = onNavigateToResources,
                )

                SetupStepItem(
                    title = stringResource(R.string.home_setup_step_chart_info),
                    done = summary.chartInfoCount > 0,
                    onClick = onNavigateToResources,
                )
            }

            DatabaseReadiness.PARTIAL -> {
                TextItem(
                    title =
                        pluralStringResource(
                            R.plurals.resources_missing_chart_info,
                            summary.missingChartInfoCount,
                            summary.missingChartInfoCount,
                        ),
                    content = stringResource(R.string.home_setup_update_chart_info),
                    leadingIcon = Icons.Default.CloudDownload,
                    trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                    onClick = onNavigateToResources,
                )
            }

            DatabaseReadiness.READY -> {
                val songCountText =
                    pluralStringResource(R.plurals.database_song_entries, summary.songCount, summary.songCount)
                val chartInfoCountText =
                    pluralStringResource(R.plurals.database_chart_info_entries, summary.chartInfoCount, summary.chartInfoCount)

                TextItem(
                    title = "$songCountText · $chartInfoCountText",
                    content =
                        uiState.lastImportedAt?.let {
                            stringResource(R.string.resources_last_imported_at, it.formatAsLocalizedDateTime())
                        },
                    leadingIcon = Icons.Default.CheckCircle,
                )
            }
        }
    }
}
