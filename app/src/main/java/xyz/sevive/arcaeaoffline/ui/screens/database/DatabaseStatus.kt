package xyz.sevive.arcaeaoffline.ui.screens.database

import androidx.annotation.PluralsRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.components.IconRow
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme


@Composable
private fun DatabaseStatusIconRow(
    icon: ImageVector,
    text: String,
    localizedItemCount: Int? = null,
    deletedItemCount: Int? = null,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_padding)),
        verticalAlignment = Alignment.Bottom,
    ) {
        IconRow {
            Icon(icon, contentDescription = null)
            Text(text)
        }

        localizedItemCount?.let {
            Text("·")

            IconRow {
                Icon(Icons.Default.Translate, contentDescription = null)
                Text(it.toString())
            }
        }

        deletedItemCount?.let {
            Text("·")

            IconRow {
                Icon(Icons.Default.DeleteForever, contentDescription = null)
                Text(it.toString())
            }
        }
    }
}

@Composable
private fun DatabaseStatusLabel(@PluralsRes labelId: Int, count: Int) {
    Text(text = pluralStringResource(id = labelId, count = count, count))
}

@Composable
fun DatabaseStatus(
    uiState: DatabaseNavEntryViewModel.StatusUiState,
    modifier: Modifier = Modifier,
) {
    with(uiState) {
        val databaseVersionText =
            remember(uiState) { "v$databaseVersion, sv$databaseSchemaVersion" }
        val packCountText =
            pluralStringResource(R.plurals.database_pack_entries, packCount, packCount)
        val songCountText =
            pluralStringResource(R.plurals.database_song_entries, songCount, songCount)
        val difficultyCountText = pluralStringResource(
            R.plurals.database_difficulty_entries, difficultyCount, difficultyCount
        )

        Card(modifier) {
            Column(
                Modifier.padding(dimensionResource(R.dimen.card_padding)),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_padding)),
            ) {
                DatabaseStatusIconRow(
                    icon = ImageVector.vectorResource(R.drawable.ic_database),
                    text = databaseVersionText,
                )

                DatabaseStatusIconRow(
                    icon = ImageVector.vectorResource(R.drawable.ic_pack),
                    text = packCountText,
                    localizedItemCount = packLocalizedCount,
                )

                DatabaseStatusIconRow(
                    icon = Icons.Default.MusicNote,
                    text = songCountText,
                    localizedItemCount = songLocalizedCount,
                    deletedItemCount = songDeletedInGameCount,
                )

                DatabaseStatusIconRow(
                    icon = ImageVector.vectorResource(R.drawable.ic_rating_class),
                    text = difficultyCountText,
                    localizedItemCount = difficultyLocalizedCount,
                )

                DatabaseStatusLabel(R.plurals.database_chart_info_entries, chartInfoCount)
                DatabaseStatusLabel(R.plurals.database_play_result_entries, playResultCount)
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun DatabaseStatusPreview() {
    ArcaeaOfflineTheme {
        Surface {
            DatabaseStatus(
                DatabaseNavEntryViewModel.StatusUiState(
                    databaseVersion = 5,
                    databaseSchemaVersion = 10,
                    packCount = 12,
                    songCount = 50,
                    difficultyCount = 153,
                    chartInfoCount = 86,
                    playResultCount = 61,
                    packLocalizedCount = 12,
                    songLocalizedCount = 34,
                    difficultyLocalizedCount = 0,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
