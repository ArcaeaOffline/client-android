package xyz.sevive.arcaeaoffline.core.search

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import xyz.sevive.arcaeaoffline.core.database.entities.DifficultyWithSong
import xyz.sevive.arcaeaoffline.core.database.entities.Pack
import xyz.sevive.arcaeaoffline.core.database.entities.PackLocalized
import xyz.sevive.arcaeaoffline.core.database.entities.Song
import xyz.sevive.arcaeaoffline.core.database.entities.SongLocalized
import xyz.sevive.arcaeaoffline.core.database.repositories.DifficultyWithSongRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.PackLocalizedRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.PackRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.SongLocalizedRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.SongRepository

/**
 * An entry of [ArcaeaCatalog]: the song, its pack and the prebuilt
 * [SearchDocument].
 */
data class ArcaeaCatalogSong(
    val song: Song,
    val pack: Pack?,
    val document: SearchDocument,
)

/**
 * An immutable snapshot of packs, songs and difficulty metadata.
 *
 * Search and pickers run against this snapshot instead of querying Room per
 * keystroke. [isLoaded] distinguishes "the database is empty" from "the first
 * emission has not arrived yet".
 */
data class ArcaeaCatalog(
    val songs: List<ArcaeaCatalogSong> = emptyList(),
    val packs: List<Pack> = emptyList(),
    val documents: List<SearchDocument> = emptyList(),
    val songsById: Map<String, ArcaeaCatalogSong> = emptyMap(),
    val packsById: Map<String, Pack> = emptyMap(),
    val songIdsByBasePackId: Map<String, Set<String>> = emptyMap(),
    val isLoaded: Boolean = false,
) {
    /**
     * Packs the picker offers as filter entries. Append packs are folded into
     * their base pack, so only packs without a reachable base are listed here.
     */
    val basePacks: List<Pack> =
        packs
            .filter { it.basePackId() == null || it.basePackId() !in packsById }
            .sortedBy { it.name.lowercase() }

    val appendPacksByBasePackId: Map<String, List<Pack>> =
        packs
            .mapNotNull { pack -> pack.basePackId()?.let { it to pack } }
            .groupBy({ it.first }, { it.second })

    /**
     * Search documents for [basePacks], including each base pack's append pack
     * names. The pack picker matches its query against these.
     */
    val packDocuments: List<SearchDocument> =
        basePacks.map { pack ->
            searchDocument(pack.id) {
                text(SearchFields.PackName, pack.name)
                text(SearchFields.PackId, pack.id)
                appendPacksByBasePackId[pack.id].orEmpty().forEach { append ->
                    text(SearchFields.PackName, append.name)
                }
            }
        }

    fun song(songId: String?): ArcaeaCatalogSong? = songId?.let { songsById[it] }

    fun pack(packId: String?): Pack? = packId?.let { packsById[it] }

    fun packName(packId: String?): String? = pack(packId)?.name

    /**
     * Song ids belonging to [basePackIds], including each base pack's append
     * packs. A song whose own pack row is missing falls back to the raw
     * `song.set` value for grouping.
     */
    fun songIdsOfBasePacks(basePackIds: Set<String>): Set<String> =
        basePackIds.flatMapTo(mutableSetOf()) { songIdsByBasePackId[it].orEmpty() }

    companion object {
        val Empty = ArcaeaCatalog()
    }
}

/**
 * Builds [ArcaeaCatalog] from the database flows and republishes it whenever any
 * of the observed tables change.
 *
 * Room invalidates the queries per table, so a songlist import rebuilds the
 * catalog once per table, not once per row. Play-result writes do not touch
 * these tables and therefore leave the catalog alone.
 */
class ArcaeaCatalogStateHolder(
    songRepo: SongRepository,
    packRepo: PackRepository,
    difficultyWithSongRepo: DifficultyWithSongRepository,
    songLocalizedRepo: SongLocalizedRepository,
    packLocalizedRepo: PackLocalizedRepository,
    scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    val catalog: StateFlow<ArcaeaCatalog> =
        combine(
            songRepo.findAll(),
            packRepo.findAll(),
            difficultyWithSongRepo.findAll(),
            songLocalizedRepo.findAll(),
            packLocalizedRepo.findAll(),
        ) { songs, packs, difficulties, songLocalized, packLocalized ->
            buildCatalog(songs, packs, difficulties, songLocalized, packLocalized)
        }.flowOn(Dispatchers.Default)
            .stateIn(scope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), ArcaeaCatalog.Empty)

    companion object {
        private const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}

private fun buildCatalog(
    songs: List<Song>,
    packs: List<Pack>,
    difficulties: List<DifficultyWithSong>,
    songLocalized: List<SongLocalized>,
    packLocalized: List<PackLocalized>,
): ArcaeaCatalog {
    val packsById = packs.associateBy { it.id }
    val difficultiesBySongId = difficulties.groupBy { it.songId }
    val localizedTitlesBySongId =
        songLocalized
            .groupBy({ it.id }, { it.title })
            .mapValues { (_, titles) -> titles.filterNotNull() }
    val localizedPackNamesByPackId =
        packLocalized
            .groupBy({ it.id }, { it.name })
            .mapValues { (_, names) -> names.filterNotNull() }

    val catalogSongs =
        songs.map { song ->
            val pack = packsById[song.set]
            val songDifficulties = difficultiesBySongId[song.id].orEmpty()
            val localizedTitles = localizedTitlesBySongId[song.id].orEmpty()
            val localizedPackNames = pack?.let { localizedPackNamesByPackId[it.id] }.orEmpty()

            ArcaeaCatalogSong(
                song = song,
                pack = pack,
                document = buildDocument(song, songDifficulties, pack, localizedTitles, localizedPackNames),
            )
        }

    val songIdsByBasePackId =
        catalogSongs
            .groupBy({ packsById[it.song.set]?.basePackId() ?: it.song.set }, { it.song.id })
            .mapValues { (_, songIds) -> songIds.toSet() }

    return ArcaeaCatalog(
        songs = catalogSongs,
        packs = packs,
        documents = catalogSongs.map { it.document },
        songsById = catalogSongs.associateBy { it.song.id },
        packsById = packsById,
        songIdsByBasePackId = songIdsByBasePackId,
        isLoaded = true,
    )
}

private fun buildDocument(
    song: Song,
    difficulties: List<DifficultyWithSong>,
    pack: Pack?,
    localizedTitles: List<String>,
    localizedPackNames: List<String>,
): SearchDocument =
    searchDocument(song.id) {
        text(SearchFields.Title, song.title)
        text(SearchFields.Artist, song.artist)
        text(SearchFields.SongId, song.id)

        // Difficulty entries may override the song title and artist. The chart
        // card displays those overrides, so they stay searchable; separate keys
        // keep their ranges away from the song-level text the row displays.
        difficulties.forEach { difficulty ->
            if (difficulty.title != song.title) text(SearchFields.ChartTitle, difficulty.title)
            if (difficulty.artist != song.artist) text(SearchFields.ChartArtist, difficulty.artist)
        }

        localizedTitles.forEach { text(SearchFields.LocalizedTitle, it) }

        pack?.let {
            text(SearchFields.PackName, it.name)
            text(SearchFields.PackId, it.id)
        }

        localizedPackNames.forEach { text(SearchFields.LocalizedPackName, it) }
    }
