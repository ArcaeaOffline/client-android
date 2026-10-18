package xyz.sevive.arcaeaoffline.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.koinInject
import xyz.sevive.arcaeaoffline.core.search.ArcaeaCatalogStateHolder
import xyz.sevive.arcaeaoffline.ui.components.arcaea.ArcaeaSongSelection
import xyz.sevive.arcaeaoffline.ui.components.arcaea.ArcaeaPackAndSongQuickSearch as SharedArcaeaPackAndSongQuickSearch

/**
 * Koin-backed wrapper that passes the injected catalog to the shared quick
 * search.
 */
@Composable
fun ArcaeaPackAndSongQuickSearch(
    onSelect: (ArcaeaSongSelection) -> Unit,
    modifier: Modifier = Modifier,
    catalogHolder: ArcaeaCatalogStateHolder = koinInject(),
) {
    val catalog by catalogHolder.catalog.collectAsStateWithLifecycle()

    SharedArcaeaPackAndSongQuickSearch(
        catalog = catalog,
        onSelect = onSelect,
        modifier = modifier,
    )
}
