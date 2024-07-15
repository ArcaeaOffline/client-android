package xyz.sevive.arcaeaoffline.core.database.helpers

import xyz.sevive.arcaeaoffline.core.database.entities.Chart
import xyz.sevive.arcaeaoffline.core.database.entities.Difficulty
import xyz.sevive.arcaeaoffline.core.database.entities.Song

object ChartFactory {
    fun fakeChart(song: Song, difficulty: Difficulty): Chart {
        return Chart(
            songIdx = song.idx,
            songId = song.id,
            ratingClass = difficulty.ratingClass,
            rating = difficulty.rating,
            ratingPlus = difficulty.ratingPlus,
            title = difficulty.title ?: song.title,
            artist = difficulty.artist ?: song.artist,
            set = song.set,
            bpm = difficulty.bpm ?: song.bpm,
            bpmBase = difficulty.bpmBase ?: song.bpmBase,
            audioPreview = song.audioPreview,
            audioPreviewEnd = song.audioPreviewEnd,
            side = song.side,
            version = difficulty.version ?: song.version,
            date = difficulty.date ?: song.date,
            bg = difficulty.bg ?: song.bg,
            bgInverse = difficulty.bgInverse ?: song.bgInverse,
            bgDay = song.bgDay,
            bgNight = song.bgNight,
            source = song.source,
            sourceCopyright = song.sourceCopyright,
            chartDesigner = difficulty.chartDesigner,
            jacketDesigner = difficulty.jacketDesigner,
            audioOverride = difficulty.audioOverride,
            jacketOverride = difficulty.jacketOverride,
            jacketNight = difficulty.jacketNight,
            constant = 0,
            notes = null,
        )
    }
}
