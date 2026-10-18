package xyz.sevive.arcaeaoffline.core.search

/**
 * Matches [SearchDocument]s against a free-text query.
 *
 * A query is split on whitespace. Every token must match somewhere in the
 * document (AND). Tokens may hit different fields. Results are ranked by the
 * summed best score of each token, so an exact title beats a pack-name hit.
 */
object SearchEngine {
    private const val QUALITY_EXACT = 4
    private const val QUALITY_PREFIX = 3
    private const val QUALITY_WORD_PREFIX = 2
    private const val QUALITY_SUBSTRING = 1

    private val TOKEN_SEPARATOR = Regex("""\s+""")

    /**
     * Normalizes text before matching:
     * - full-width ASCII and the ideographic space are folded to ASCII;
     * - letters are lowercased;
     * - common Latin accents, Greek/Cyrillic lookalikes, superscript/subscript
     *   digits and a few symbols are folded to their ASCII base.
     *
     * Every mapping is one character to one character, which keeps indices in
     * [SearchDocument] fields valid for highlighting. Multi-character folds
     * (e.g. `Æ` to `ae`) are not applied.
     */
    fun normalize(input: String): String =
        buildString(input.length) {
            for (ch in input) {
                val widthFolded =
                    when (ch) {
                        in '\uFF01'..'\uFF5E' -> ch - 0xFEE0
                        '\u3000' -> ' '
                        else -> ch
                    }
                append(foldCharacter(widthFolded.lowercaseChar()))
            }
        }.trim()

    private fun foldCharacter(ch: Char): Char =
        when (ch) {
            // Latin-1 Supplement / Latin Extended-A letters.
            'à', 'á', 'â', 'ã', 'ä', 'å', 'ā', 'ă', 'ą' -> 'a'

            'ç', 'ć', 'ĉ', 'ċ', 'č' -> 'c'

            'ď', 'đ' -> 'd'

            'è', 'é', 'ê', 'ë', 'ē', 'ĕ', 'ė', 'ę', 'ě' -> 'e'

            'ĝ', 'ğ', 'ġ', 'ģ' -> 'g'

            'ĥ', 'ħ' -> 'h'

            'ì', 'í', 'î', 'ï', 'ĩ', 'ī', 'ĭ', 'į', 'ı' -> 'i'

            'ĵ' -> 'j'

            'ķ' -> 'k'

            'ĺ', 'ļ', 'ľ', 'ł' -> 'l'

            'ñ', 'ń', 'ņ', 'ň' -> 'n'

            'ò', 'ó', 'ô', 'õ', 'ö', 'ø', 'ō', 'ŏ', 'ő' -> 'o'

            'ŕ', 'ŗ', 'ř' -> 'r'

            'ś', 'ŝ', 'ş', 'š' -> 's'

            'ţ', 'ť', 'ŧ' -> 't'

            'ù', 'ú', 'û', 'ü', 'ũ', 'ū', 'ŭ', 'ů', 'ű', 'ų' -> 'u'

            'ŵ' -> 'w'

            'ý', 'ÿ', 'ŷ' -> 'y'

            'ź', 'ż', 'ž' -> 'z'

            // Greek letters used as Latin lookalikes. Λ/λ map to A rather than
            // the transliteration L because titles use them stylistically
            // (e.g. ΛZΛLEΛ). A real Greek word containing Λ would fold incorrectly.
            'α', 'ά', 'λ' -> 'a'

            'β' -> 'b'

            'γ' -> 'g'

            'δ' -> 'd'

            'ε', 'έ' -> 'e'

            'ι', 'ί', 'ϊ', 'ΐ' -> 'i'

            'κ' -> 'k'

            'μ' -> 'm'

            'ο', 'ό' -> 'o'

            'σ', 'ς' -> 's'

            'τ' -> 't'

            'υ', 'ύ', 'ϋ', 'ΰ' -> 'y'

            'ω', 'ώ' -> 'w'

            // Cyrillic Я used as a reversed R (VECTOЯ).
            'я' -> 'r'

            // Superscript and subscript digits.
            '⁰', '₀' -> '0'

            '¹', '₁' -> '1'

            '²', '₂' -> '2'

            '³', '₃' -> '3'

            '⁴', '₄' -> '4'

            '⁵', '₅' -> '5'

            '⁶', '₆' -> '6'

            '⁷', '₇' -> '7'

            '⁸', '₈' -> '8'

            '⁹', '₉' -> '9'

            // Symbols typed as ASCII.
            '×' -> 'x'

            '〜' -> '~'

            else -> ch
        }

    fun tokenize(query: String): List<String> = normalize(query).split(TOKEN_SEPARATOR).filter { it.isNotEmpty() }

    /**
     * Returns the matching documents ranked best first, at most [limit] of them.
     *
     * Highlights are computed for the best-scoring field of every token.
     */
    fun search(
        documents: List<SearchDocument>,
        query: String,
        limit: Int = Int.MAX_VALUE,
    ): List<SearchMatch> {
        val tokens = tokenize(query)
        if (tokens.isEmpty() || limit <= 0) return emptyList()

        val scored = ArrayList<Pair<SearchDocument, SearchMatch>>()
        for (document in documents) {
            val match = matchDocument(document, tokens) ?: continue
            scored += document to match
        }

        scored.sortWith(
            compareByDescending<Pair<SearchDocument, SearchMatch>> { it.second.score }
                .thenBy { it.first.tieBreaker.length }
                .thenBy { it.first.tieBreaker },
        )

        return scored.take(limit).map { it.second }
    }

    /**
     * Returns the ids of the matching documents, without ranking or highlights.
     *
     * This is the bulk-filter path: it skips the per-match allocations that
     * [search] needs for display.
     */
    fun searchIds(
        documents: List<SearchDocument>,
        query: String,
    ): Set<String> {
        val tokens = tokenize(query)
        if (tokens.isEmpty()) return emptySet()

        val ids = HashSet<String>()
        for (document in documents) {
            if (matchesAll(document, tokens)) ids += document.id
        }
        return ids
    }

    private val SearchDocument.tieBreaker: String
        get() = fields.firstOrNull()?.original.orEmpty()

    private fun matchesAll(
        document: SearchDocument,
        tokens: List<String>,
    ): Boolean {
        for (token in tokens) {
            if (document.fields.none { matchToken(token, it.normalized) != null }) return false
        }
        return true
    }

    private fun matchDocument(
        document: SearchDocument,
        tokens: List<String>,
    ): SearchMatch? {
        var total = 0
        val highlights = LinkedHashMap<String, MutableList<IntRange>>()

        for (token in tokens) {
            var bestScore = 0
            var bestMatch: TokenMatch? = null
            var bestField: SearchFieldText? = null

            for (field in document.fields) {
                val match = matchToken(token, field.normalized) ?: continue
                val score = match.quality * field.field.weight
                if (score > bestScore) {
                    bestScore = score
                    bestMatch = match
                    bestField = field
                }
            }

            val field = bestField ?: return null
            val match = bestMatch ?: return null
            total += bestScore
            addHighlight(highlights, field, match.index, token.length)
        }

        return SearchMatch(
            documentId = document.id,
            score = total,
            highlights = highlights.mapValues { (_, ranges) -> ranges.distinct().sortedBy { it.first } },
        )
    }

    private fun addHighlight(
        highlights: MutableMap<String, MutableList<IntRange>>,
        field: SearchFieldText,
        index: Int,
        length: Int,
    ) {
        if (index < 0) return
        if (field.original.length != field.normalized.length) return

        val end = index + length
        if (end > field.original.length) return

        highlights.getOrPut(field.field.key) { mutableListOf() } += index until end
    }

    private data class TokenMatch(
        val quality: Int,
        val index: Int,
    )

    private fun matchToken(
        token: String,
        text: String,
    ): TokenMatch? {
        if (token.isEmpty() || text.isEmpty()) return null
        if (text == token) return TokenMatch(QUALITY_EXACT, 0)
        if (text.startsWith(token)) return TokenMatch(QUALITY_PREFIX, 0)

        var substringIndex = -1
        var from = 0
        while (true) {
            val index = text.indexOf(token, from)
            if (index < 0) break

            if (substringIndex < 0) substringIndex = index
            if (index == 0 || !text[index - 1].isLetterOrDigit()) {
                return TokenMatch(QUALITY_WORD_PREFIX, index)
            }
            from = index + 1
        }

        return if (substringIndex >= 0) TokenMatch(QUALITY_SUBSTRING, substringIndex) else null
    }
}
