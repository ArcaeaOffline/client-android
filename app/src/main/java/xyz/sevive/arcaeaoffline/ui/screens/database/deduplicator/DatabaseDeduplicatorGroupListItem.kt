package xyz.sevive.arcaeaoffline.ui.screens.database.deduplicator

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Merge
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.helpers.ArcaeaPlayResultValidator
import xyz.sevive.arcaeaoffline.ui.components.ArcaeaChartCard
import xyz.sevive.arcaeaoffline.ui.components.ArcaeaPlayResultCard
import xyz.sevive.arcaeaoffline.ui.components.ArcaeaPlayResultEditorDialog
import xyz.sevive.arcaeaoffline.ui.components.ListGroupHeader
import java.util.UUID


@Composable
internal fun DatabaseDeduplicatorGroupListItem(
    item: DatabaseDeduplicatorViewModel.GroupListUiItem,
    selectedUuids: Set<UUID>,
    onPlayResultSelectedChange: (UUID, Boolean) -> Unit,
    onMergeConfirm: (PlayResult) -> Unit,
) {
    var mergedPlayResult by remember { mutableStateOf<PlayResult?>(null) }

    var showPlayResultEditorDialog by rememberSaveable { mutableStateOf(false) }
    if (showPlayResultEditorDialog) {
        mergedPlayResult?.let { playResult ->
            ArcaeaPlayResultEditorDialog(
                onDismiss = {
                    mergedPlayResult = null
                    showPlayResultEditorDialog = false
                },
                playResult = playResult,
                onPlayResultChange = { onMergeConfirm(it) },
            )
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.icon_text_padding))) {
        Row(verticalAlignment = Alignment.Bottom) {
            ListGroupHeader(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(item.index.toString(), fontWeight = FontWeight.Bold)
                    Text(
                        item.key,
                        Modifier.padding(start = dimensionResource(R.dimen.icon_text_padding)),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Light,
                    )
                }
            }

            IconButton(
                onClick = {
                    mergedPlayResult = item.playResults.mergePlayResults()
                    showPlayResultEditorDialog = true
                },
            ) {
                Icon(Icons.Default.Merge, contentDescription = null)
            }
        }

        AnimatedVisibility(visible = item.chart != null) {
            item.chart?.let {
                ArcaeaChartCard(it)
            }
        }

        item.playResults.forEach {
            val selected = selectedUuids.contains(it.uuid)
            val onSelect = { onPlayResultSelectedChange(it.uuid, !selected) }
            val warnings = remember(it, item.chart) {
                ArcaeaPlayResultValidator.validate(it, item.chart)
            }

            Row(
                Modifier.clickable(onClick = onSelect),
                verticalAlignment = Alignment.Bottom,
            ) {
                ArcaeaPlayResultCard(
                    it,
                    Modifier.weight(1f),
                    warnings = warnings,
                    onClick = onSelect,
                )

                Checkbox(
                    checked = selected,
                    onCheckedChange = null,
                    Modifier.minimumInteractiveComponentSize(),
                )
            }
        }
    }
}
