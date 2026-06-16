package xyz.sevive.arcaeaoffline.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.plus
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.firstOrNull
import org.koin.compose.koinInject
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.database.entities.Pack
import xyz.sevive.arcaeaoffline.core.database.entities.Song
import xyz.sevive.arcaeaoffline.core.database.repositories.PackRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.SongRepository

data class FilterResult(
    val packId: String,
    val songId: String,
)

private data class Candidate(
    val pack: Pack,
    val song: Song,
)

@Composable
private fun rememberCandidates(
    searchInput: String,
    packRepo: PackRepository,
    songRepo: SongRepository,
): State<List<Candidate>> {
    return produceState(initialValue = emptyList(), searchInput) {
        if (searchInput.length <= 1) {
            value = emptyList()
            return@produceState
        }

        // search exact matches first
        val songIdExact = songRepo.find(id = searchInput).firstOrNull()

        // then search possible title matches
        val songs = songRepo.searchByTitle(searchInput).firstOrNull() ?: emptyList()

        // if any of the pack could be matched, add it too
        val packExact = packRepo.find(id = searchInput).firstOrNull()
        val packCandidates = packRepo.searchByName(searchInput).firstOrNull() ?: emptyList()

        // now organize our result, the priority is
        // exact songId > song titles > exact packId > pack name matches
        val candidates = mutableListOf<Candidate>()
        val packsMap = mutableMapOf<String, Pack>()
        val addedSongIds = mutableSetOf<String>()

        suspend fun addCandidate(song: Song) {
            if (song.id in addedSongIds) return

            val pack = packsMap[song.set] ?: packRepo.find(song.set).firstOrNull() ?: return
            packsMap[song.id] = pack
            addedSongIds.add(song.id)
            candidates.add(Candidate(pack = pack, song = song))
        }

        // 1. exact songId
        songIdExact?.let { addCandidate(it) }

        // 2. song titles
        songs.forEach { addCandidate(it) }

        // 3. exact packId
        packExact?.let { pack ->
            songRepo.findBySet(pack.id).firstOrNull()?.forEach { addCandidate(it) }
        }

        // 4. pack name matches
        packCandidates.forEach { pack ->
            songRepo.findBySet(pack.id).firstOrNull()?.forEach { addCandidate(it) }
        }

        value = candidates
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArcaeaPackAndSongInputFilter(
    onSelect: (FilterResult) -> Unit,
    modifier: Modifier = Modifier,
    packRepo: PackRepository = koinInject(),
    songRepo: SongRepository = koinInject(),
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedOptionText by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    val candidates by rememberCandidates(selectedOptionText, packRepo, songRepo)

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier,
    ) {
        OutlinedTextField(
            // set PrimaryEditable so the popup menu won't focus, avoiding overlap with soft keyboard
            modifier =
                Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
                    .fillMaxWidth(),
            value = selectedOptionText,
            onValueChange = {
                selectedOptionText = it
                expanded = true
            },
            label = { Text(stringResource(R.string.song_id_selector_quick_search)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
        )

        if (candidates.isNotEmpty()) {
            ExposedDropdownMenu(
                modifier =
                    Modifier
                        .exposedDropdownSize(true)
                        // TODO: more elegant way?
                        .heightIn(max = 250.dp),
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                candidates.forEachIndexed { i, candidate ->
                    DropdownMenuItem(
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(candidate.song.title, style = MaterialTheme.typography.labelLarge)
                                Text(
                                    candidate.pack.name,
                                    color = MaterialTheme.colorScheme.secondary,
                                    style = MaterialTheme.typography.labelMedium,
                                )
                            }
                        },
                        onClick = {
                            onSelect(FilterResult(candidate.pack.id, candidate.song.id))
                            selectedOptionText = ""
                            expanded = false
                            focusManager.clearFocus()
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding.plus(PaddingValues(vertical = 8.dp)),
                    )

                    if (i != candidates.lastIndex) {
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
