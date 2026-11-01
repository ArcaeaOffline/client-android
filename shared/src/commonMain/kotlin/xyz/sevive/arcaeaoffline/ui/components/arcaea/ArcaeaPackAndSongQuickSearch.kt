package xyz.sevive.arcaeaoffline.ui.components.arcaea

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import xyz.sevive.arcaeaoffline.core.search.ArcaeaCatalog
import xyz.sevive.arcaeaoffline.core.search.ArcaeaCatalogSong
import xyz.sevive.arcaeaoffline.core.search.SearchEngine
import xyz.sevive.arcaeaoffline.core.search.SearchMatch
import xyz.sevive.arcaeaoffline.resources.Res
import xyz.sevive.arcaeaoffline.resources.general_no_results
import xyz.sevive.arcaeaoffline.resources.general_please_wait
import xyz.sevive.arcaeaoffline.resources.song_id_selector_quick_search
import xyz.sevive.arcaeaoffline.ui.components.ArcaeaSearchTextField

/**
 * A selected song and the pack it belongs to.
 */
data class ArcaeaSongSelection(
    val packId: String,
    val songId: String,
)

private const val QUICK_SEARCH_LIMIT = 8

/**
 * Quick song lookup for forms that need to pick one chart.
 *
 * Matching runs against the in-memory catalog, so typing costs no database
 * queries. Results show title, artist and pack with the matched fragments
 * highlighted. The IME search action selects the first result.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArcaeaPackAndSongQuickSearch(
    catalog: ArcaeaCatalog,
    onSelect: (ArcaeaSongSelection) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current

    var query by rememberSaveable { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val results: List<SearchMatch> =
        remember(query, catalog) {
            if (query.isBlank()) {
                emptyList()
            } else {
                SearchEngine.search(catalog.documents, query, limit = QUICK_SEARCH_LIMIT)
            }
        }

    fun updateQuery(value: String) {
        query = value
        expanded = value.isNotBlank()
    }

    fun select(song: ArcaeaCatalogSong) {
        onSelect(ArcaeaSongSelection(packId = song.pack?.id ?: song.song.set, songId = song.song.id))
        query = ""
        expanded = false
        focusManager.clearFocus()
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it && query.isNotBlank() },
        modifier = modifier,
    ) {
        ArcaeaSearchTextField(
            value = query,
            onValueChange = { updateQuery(it) },
            placeholder = stringResource(Res.string.song_id_selector_quick_search),
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable),
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { updateQuery("") }) {
                        Icon(Icons.Default.Close, contentDescription = null)
                    }
                } else {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            },
            onSearch = { results.firstOrNull()?.let { catalog.song(it.documentId)?.let(::select) } },
        )

        ExposedDropdownMenu(
            modifier =
                Modifier
                    .exposedDropdownSize(true)
                    .heightIn(max = 360.dp),
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            when {
                !catalog.isLoaded -> {
                    DropdownMenuItem(
                        text = { Text(stringResource(Res.string.general_please_wait)) },
                        onClick = {},
                        enabled = false,
                    )
                }

                results.isEmpty() -> {
                    DropdownMenuItem(
                        text = { Text(stringResource(Res.string.general_no_results)) },
                        onClick = {},
                        enabled = false,
                    )
                }

                else -> {
                    results.forEachIndexed { index, match ->
                        val entry = catalog.song(match.documentId) ?: return@forEachIndexed

                        DropdownMenuItem(
                            text = { ArcaeaSongSearchRow(entry = entry, match = match) },
                            onClick = { select(entry) },
                            // The row supplies its own horizontal padding.
                            contentPadding = PaddingValues(0.dp),
                        )

                        if (index != results.lastIndex) {
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}
