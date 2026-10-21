package xyz.sevive.arcaeaoffline.core.search

/**
 * A searchable field. [weight] scales every match on the field, so a hit on the
 * song title ranks above the same hit on the pack name.
 */
data class SearchField(
    val key: String,
    val weight: Int,
)

/**
 * One searchable text of a [SearchDocument].
 *
 * [original] is what the UI displays. [normalized] is the opaque encoding
 * [SearchEngine] matches against: one character per [original] character.
 * Highlight indices returned by the engine refer to [original].
 */
data class SearchFieldText(
    val field: SearchField,
    val original: String,
    val normalized: String,
)

/**
 * A searchable entity. [id] is opaque to the engine and only echoed back.
 *
 * The first field is used as the tie-breaker between equal scores, so build the
 * document with the most representative text (the title) first.
 */
data class SearchDocument(
    val id: String,
    val fields: List<SearchFieldText>,
)

/**
 * A matched document.
 *
 * [highlights] maps [SearchField.key] to half-open index ranges in the field's
 * original text.
 */
data class SearchMatch(
    val documentId: String,
    val score: Int,
    val highlights: Map<String, List<IntRange>>,
)

/**
 * Field definitions used by the catalog builder and the UIs that read highlight
 * ranges by key.
 *
 * Title and artist overrides from difficulty entries use their own keys so
 * their highlight ranges do not collide with the song-level text the search
 * row displays.
 */
object SearchFields {
    val Title = SearchField(key = "title", weight = 100)

    val LocalizedTitle = SearchField(key = "localized_title", weight = 95)

    val SongId = SearchField(key = "song_id", weight = 90)

    val Artist = SearchField(key = "artist", weight = 55)

    val ChartTitle = SearchField(key = "chart_title", weight = 100)

    val ChartArtist = SearchField(key = "chart_artist", weight = 55)

    val PackName = SearchField(key = "pack_name", weight = 45)

    val LocalizedPackName = SearchField(key = "localized_pack_name", weight = 45)

    val PackId = SearchField(key = "pack_id", weight = 40)
}

/**
 * Builds a [SearchDocument] while normalizing and de-duplicating texts.
 */
class SearchDocumentBuilder internal constructor(
    private val id: String,
) {
    private val fields = mutableListOf<SearchFieldText>()

    /**
     * Adds [value] under [field]. Blank values and repeats of an already added
     * (field, encoded text) pair are ignored: the same title can come from both
     * the song and a difficulty override.
     */
    fun text(
        field: SearchField,
        value: String?,
    ) {
        if (value.isNullOrBlank()) return

        val normalized = SearchEngine.encodeClasses(value)
        if (fields.any { it.field.key == field.key && it.normalized == normalized }) return

        fields += SearchFieldText(field = field, original = value, normalized = normalized)
    }

    internal fun build(): SearchDocument = SearchDocument(id = id, fields = fields)
}

fun searchDocument(
    id: String,
    block: SearchDocumentBuilder.() -> Unit,
): SearchDocument = SearchDocumentBuilder(id).apply(block).build()
