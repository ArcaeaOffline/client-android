package xyz.sevive.arcaeaoffline.core.database.externals.importers

import android.util.Log
import kotlinx.serialization.json.Json
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaLanguage
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.database.entities.Difficulty
import xyz.sevive.arcaeaoffline.core.database.entities.DifficultyLocalized
import xyz.sevive.arcaeaoffline.core.database.entities.Song
import xyz.sevive.arcaeaoffline.core.database.entities.SongLocalized
import xyz.sevive.arcaeaoffline.core.database.externals.ArcaeaSonglistRoot


class ArcaeaSonglistImporter(songlistContent: String) {
    companion object {
        const val LOG_TAG = "ArcaeaSlstImporter"

        private val LANGUAGES = listOf(
            ArcaeaLanguage.JA, ArcaeaLanguage.KO, ArcaeaLanguage.ZH_HANS, ArcaeaLanguage.ZH_HANT
        )
    }

    private val json = Json { ignoreUnknownKeys = true }
    private val contentDecoded = json.decodeFromString<ArcaeaSonglistRoot>(songlistContent)

    fun songs(): List<Song> {
        val items = mutableListOf<Song>()
        for (song in contentDecoded.songs) {
            items.add(
                Song(
                    idx = song.idx,
                    id = song.id,
                    title = song.titleLocalized.en ?: "",
                    artist = song.artist,
                    set = song.set,
                    bpm = song.bpm,
                    bpmBase = song.bpmBase,
                    audioPreview = song.audioPreview,
                    audioPreviewEnd = song.audioPreviewEnd,
                    side = song.side,
                    version = song.version,
                    date = song.date,
                    bg = song.bg,
                    bgInverse = song.bgInverse,
                    bgDay = song.bgDayNight?.day,
                    bgNight = song.bgDayNight?.night,
                    source = song.sourceLocalized?.en,
                    sourceCopyright = song.sourceCopyright,
                )
            )
        }
        return items
    }

    fun songsLocalized(): List<SongLocalized> {
        val items = mutableListOf<SongLocalized>()

        for (song in contentDecoded.songs) {
            for (lang in LANGUAGES) {
                song.getSongLocalized(lang)?.let { items.add(it) }
            }
        }

        return items
    }

    fun difficulties(): List<Difficulty> {
        val items = mutableListOf<Difficulty>()

        for (song in contentDecoded.songs) {
            for (difficulty in song.difficulties) {
                if (difficulty.rating == 0) {
                    Log.d(
                        LOG_TAG, "Skipping ${song.id}.${difficulty.ratingClass}: rating is 0"
                    )
                    continue
                }

                items.add(
                    Difficulty(
                        songId = song.id,
                        ratingClass = ArcaeaRatingClass.fromInt(difficulty.ratingClass),
                        rating = difficulty.rating,
                        ratingPlus = difficulty.ratingPlus ?: false,
                        chartDesigner = difficulty.chartDesigner,
                        jacketDesigner = difficulty.jacketDesigner,
                        audioOverride = difficulty.audioOverride ?: false,
                        jacketOverride = difficulty.jacketOverride ?: false,
                        jacketNight = difficulty.jacketNight,
                        title = difficulty.titleLocalized?.en,
                        artist = difficulty.artist,
                        bg = difficulty.bg,
                        bgInverse = difficulty.bgInverse,
                        bpm = difficulty.bpm,
                        bpmBase = difficulty.bpmBase,
                        version = difficulty.version,
                        date = difficulty.date,
                    )
                )
            }
        }

        return items
    }

    fun difficultiesLocalized(): List<DifficultyLocalized> {
        val items = mutableListOf<DifficultyLocalized>()

        for (song in contentDecoded.songs) {
            for (difficulty in song.difficulties) {
                for (lang in LANGUAGES) {
                    if (difficulty.rating == 0) {
                        Log.d(
                            LOG_TAG, "Skipping ${song.id}.${difficulty.ratingClass}: rating is 0"
                        )
                        continue
                    }

                    difficulty.getDifficultyLocalized(song.id, lang)?.let { items.add(it) }
                }
            }
        }

        return items
    }
}
