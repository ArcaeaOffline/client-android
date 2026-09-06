package xyz.sevive.arcaeaoffline.core.database.daos

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.database.entities.DifficultyWithSong
import xyz.sevive.arcaeaoffline.core.database.entities.DifficultyWithSongAndInfo

// Keep in sync with DifficultyWithSong's fields; COALESCE falls back to the
// song-level metadata when the difficulty entry does not override it.
private const val DIFFICULTY_WITH_SONG_QUERY_BASE =
    """
    SELECT
        d.song_id, d.rating_class, d.rating_class_alias,
        d.rating, d.rating_plus,
        COALESCE(d.title, s.title) AS title,
        COALESCE(d.artist, s.artist) AS artist
    FROM difficulties d
    INNER JOIN songs s ON d.song_id = s.id
    """

private const val DIFFICULTY_WITH_SONG_AND_INFO_QUERY =
    """
    SELECT
        d.song_id, d.rating_class, d.rating_class_alias,
        d.rating, d.rating_plus,
        COALESCE(d.title, s.title) AS title,
        COALESCE(d.artist, s.artist) AS artist,
        ci.constant, ci.notes
    FROM difficulties d
    INNER JOIN songs s ON d.song_id = s.id
    INNER JOIN charts_info ci ON d.song_id = ci.song_id AND d.rating_class = ci.rating_class
    ORDER BY ci.constant
    """

@Dao
interface DifficultyWithSongDao {
    @Query("$DIFFICULTY_WITH_SONG_QUERY_BASE WHERE d.song_id = :songId AND d.rating_class = :ratingClass")
    fun find(
        songId: String,
        ratingClass: ArcaeaRatingClass,
    ): Flow<DifficultyWithSong?>

    @Query("$DIFFICULTY_WITH_SONG_QUERY_BASE WHERE d.song_id = :songId")
    fun findAllBySongId(songId: String): Flow<List<DifficultyWithSong>>

    @Query("$DIFFICULTY_WITH_SONG_QUERY_BASE WHERE d.song_id IN (:songIds)")
    fun findAllBySongIds(songIds: List<String>): Flow<List<DifficultyWithSong>>

    // Only rows with chart info (INNER JOIN): consumers filter and display by
    // constant, which is meaningless without it.
    @Query(DIFFICULTY_WITH_SONG_AND_INFO_QUERY)
    fun findAllWithInfo(): Flow<List<DifficultyWithSongAndInfo>>
}
