package xyz.sevive.arcaeaoffline.core.database.daos

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.database.entities.Chart

// Display columns shared by every query returning [Chart]-shaped rows; keep
// in sync with Chart's fields. RelationshipsDao reuses this fragment with its
// own identity columns, since pr.* shadows song_id/rating_class/date.
internal const val CHART_DISPLAY_COLUMNS =
    """
    COALESCE(d.title, s.title) AS title, COALESCE(d.artist, s.artist) AS artist,
    s.`set`, COALESCE(d.bpm, s.bpm) AS bpm, COALESCE(d.bpm_base, s.bpm_base) AS bpm_base,
    s.audio_preview, s.audio_preview_end, s.side,
    COALESCE(d.version, s.version) AS version, COALESCE(d.date, s.date) AS date,
    COALESCE(d.bg, s.bg) AS bg, COALESCE(d.bg_inverse, s.bg_inverse) AS bg_inverse,
    s.bg_day, s.bg_night, s.source, s.source_copyright,
    d.chart_designer, d.jacket_designer, d.audio_override, d.jacket_override,
    d.jacket_night
    """

private const val CHART_QUERY_BASE =
    """
    SELECT
        s.idx AS song_idx, d.song_id, d.rating_class, d.rating_class_alias,
        d.rating, d.rating_plus,
        $CHART_DISPLAY_COLUMNS,
        ci.constant, ci.notes
    FROM difficulties d
    INNER JOIN charts_info ci ON d.song_id = ci.song_id AND d.rating_class = ci.rating_class
    INNER JOIN songs s ON d.song_id = s.id
    """

@Dao
interface ChartDao {
    @Query("$CHART_QUERY_BASE WHERE d.song_id = :songId AND d.rating_class = :ratingClass")
    fun find(
        songId: String,
        ratingClass: ArcaeaRatingClass,
    ): Flow<Chart?>

    @Query(CHART_QUERY_BASE)
    fun findAll(): Flow<List<Chart>>

    @Query("$CHART_QUERY_BASE WHERE d.song_id = :songId")
    fun findAllBySongId(songId: String): Flow<List<Chart>>

    @Query("$CHART_QUERY_BASE WHERE d.song_id IN (:songIds)")
    fun findAllBySongIds(songIds: List<String>): Flow<List<Chart>>
}
