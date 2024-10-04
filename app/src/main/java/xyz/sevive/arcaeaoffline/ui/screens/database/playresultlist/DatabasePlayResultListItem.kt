package xyz.sevive.arcaeaoffline.ui.screens.database.playresultlist

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.ui.components.ArcaeaPlayResultCard
import xyz.sevive.arcaeaoffline.ui.components.ArcaeaPlayResultEditorDialog


@Composable
internal fun DatabasePlayResultListItem(
    item: DatabasePlayResultListViewModel.ListItem,
    onPlayResultChange: (PlayResult) -> Unit,
    inSelectMode: Boolean,
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showPlayResultEditor by rememberSaveable { mutableStateOf(false) }
    if (showPlayResultEditor) {
        ArcaeaPlayResultEditorDialog(
            onDismiss = { showPlayResultEditor = false },
            playResult = item.playResult,
            onPlayResultChange = onPlayResultChange,
        )
    }

    Row(
        modifier.clickable(inSelectMode) { onSelectedChange(!selected) },
        verticalAlignment = Alignment.Bottom,
    ) {
        ArcaeaPlayResultCard(
            playResult = item.playResult,
            modifier = Modifier.weight(1f),
            onClick = { onSelectedChange(!selected) },
            chart = item.chart,
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("PTT", fontWeight = FontWeight.Thin, style = MaterialTheme.typography.labelSmall)
            Text(item.potentialText, style = MaterialTheme.typography.labelMedium)

            Crossfade(targetState = inSelectMode, label = "listItemActionButton") {
                Box(Modifier.minimumInteractiveComponentSize()) {
                    if (it) {
                        Checkbox(checked = selected, onCheckedChange = null)
                    } else {
                        IconButton(onClick = { showPlayResultEditor = true }) {
                            Icon(Icons.Default.Edit, null)
                        }
                    }
                }
            }
        }
    }
}
