package xyz.sevive.arcaeaoffline.core.database.externals.arcaea

import android.util.Log
import kotlinx.serialization.json.Json
import xyz.sevive.arcaeaoffline.core.database.entities.Difficulty
import xyz.sevive.arcaeaoffline.core.database.entities.Pack
import xyz.sevive.arcaeaoffline.core.database.entities.Song

val format = Json { ignoreUnknownKeys = true }

class PacklistParser(private val content: String) {
    fun parsePack(): List<Pack> {
        val contentDecoded = format.decodeFromString<PacklistRoot>(content)

        val result = mutableListOf(Pack(id = "single", name = "Memory Archive"))
        for (pack in contentDecoded.packs) {
            result.add(
                Pack(
                    id = pack.id,
                    name = pack.nameLocalized.en ?: "Pack",
                    description = pack.descriptionLocalized.en
                )
            )
        }
        return result.toList()
    }
}

class SonglistParser(private val content: String) {
    fun parseSong(): List<Song> {
        val contentDecoded = format.decodeFromString<SonglistRoot>(content)

        val result = mutableListOf<Song>()
        for (song in contentDecoded.songs) {
            result.add(
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
        return result.toList()
    }

    fun parseDifficulty(): List<Difficulty> {
        val contentDecoded = format.decodeFromString<SonglistRoot>(content)

        val result = mutableListOf<Difficulty>()
        for (song in contentDecoded.songs) {
            for (difficulty in song.difficulties) {
                if (difficulty.rating == 0) {
                    Log.d(
                        "SonglistParser",
                        "Skipping ${song.id}@${difficulty.ratingClass}: rating is 0"
                    )
                    continue
                }

                result.add(
                    Difficulty(
                        songId = song.id,
                        ratingClass = difficulty.ratingClass,
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

        return result.toList()
    }
}
