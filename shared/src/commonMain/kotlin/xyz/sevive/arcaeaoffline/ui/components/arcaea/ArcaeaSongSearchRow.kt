package xyz.sevive.arcaeaoffline.ui.components.arcaea

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import xyz.sevive.arcaeaoffline.core.search.ArcaeaCatalogSong
import xyz.sevive.arcaeaoffline.core.search.SearchFields
import xyz.sevive.arcaeaoffline.core.search.SearchMatch
import xyz.sevive.arcaeaoffline.ui.components.HighlightedText
import xyz.sevive.arcaeaoffline.ui.theme.spacing

/**
 * One song in a search result list: title on the first line, artist and pack on
 * the second. Matched fragments of both lines are highlighted.
 */
@Composable
fun ArcaeaSongSearchRow(
    entry: ArcaeaCatalogSong,
    match: SearchMatch?,
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null,
) {
    val titleRanges = match?.highlights?.get(SearchFields.Title.key).orEmpty()
    val artistRanges = match?.highlights?.get(SearchFields.Artist.key).orEmpty()
    val packRanges = match?.highlights?.get(SearchFields.PackName.key).orEmpty()

    val artist = entry.song.artist
    val packName = entry.pack?.name ?: entry.song.set
    val metaText = "$artist · $packName"

    // The engine reports ranges against the individual fields, so the pack
    // ranges are shifted to their position in the combined line.
    val packOffset = artist.length + " · ".length
    val metaRanges = artistRanges + packRanges.map { (it.first + packOffset)..(it.last + packOffset) }

    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = MaterialTheme.spacing.lg, vertical = MaterialTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leading?.let {
            Box(Modifier.padding(end = MaterialTheme.spacing.sm)) { it() }
        }

        Column(Modifier.weight(1f)) {
            HighlightedText(
                text = entry.song.title,
                ranges = titleRanges,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
            )

            HighlightedText(
                text = metaText,
                ranges = metaRanges,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
