package xyz.sevive.arcaeaoffline.core.search

/**
 * Matches [SearchDocument]s against a free-text query.
 *
 * A query is split on whitespace. Every token must match somewhere in the
 * document (AND). Tokens may hit different fields. Results are ranked by the
 * summed best score of each token, so an exact title beats a pack-name hit.
 *
 * Characters match by class: a Greek letter accepts every Latin reading users
 * may type for it (γ accepts g and y; υ accepts y, u and v).
 */
object SearchEngine {
    private const val QUALITY_EXACT = 4
    private const val QUALITY_PREFIX = 3
    private const val QUALITY_WORD_PREFIX = 2
    private const val QUALITY_SUBSTRING = 1

    private val TOKEN_SEPARATOR = Regex("""\s+""")

    /**
     * Normalizes text to one canonical character per input character:
     * - full-width ASCII and the ideographic space are folded to ASCII;
     * - letters are lowercased;
     * - accented letters, Greek/Cyrillic lookalikes, superscript/subscript
     *   digits and a few symbols are folded to their first ASCII reading.
     *
     * Matching compares every reading of a character, not only this canonical
     * one (see [search]). Aliases are single characters, so multi-character
     * transliterations (e.g. θ to `th`, Æ to `ae`) are not represented.
     */
    fun normalize(input: String): String =
        buildString(input.length) {
            for (ch in input) {
                val folded = foldWidth(ch).lowercaseChar()
                append(ALIASES[folded]?.first() ?: folded)
            }
        }.trim()

    /**
     * Encodes each character as the class of characters it matches, one output
     * character per input character. Whitespace becomes a single space so
     * callers can split tokens on it.
     */
    internal fun encodeClasses(input: String): String =
        buildString(input.length) {
            for (ch in input) {
                val folded = foldWidth(ch).lowercaseChar()
                append(if (folded.isWhitespace()) ' ' else classOf(folded))
            }
        }

    private fun foldWidth(ch: Char): Char =
        when (ch) {
            in '\uFF01'..'\uFF5E' -> ch - 0xFEE0
            '\u3000' -> ' '
            else -> ch
        }

    // Each value lists the characters a source character matches; the first is
    // the canonical reading returned by normalize. Entries are single characters.
    private val ALIASES: Map<Char, String> =
        buildMap {
            fun register(
                chars: String,
                aliases: String,
            ) {
                chars.forEach { put(it, aliases) }
            }

            // Latin-1 Supplement / Latin Extended-A letters.
            register("àáâãäåāăą", "a")
            register("çćĉċč", "c")
            register("ďđ", "d")
            register("èéêëēĕėęě", "e")
            register("ĝğġģ", "g")
            register("ĥħ", "h")
            register("ìíîïĩīĭįı", "i")
            register("ĵ", "j")
            register("ķ", "k")
            register("ĺļľł", "l")
            register("ñńņň", "n")
            register("òóôõöøōŏő", "o")
            register("ŕŗř", "r")
            register("śŝşš", "s")
            register("ţťŧ", "t")
            register("ùúûüũūŭůűų", "u")
            register("ŵ", "w")
            register("ýÿŷ", "y")
            register("źżž", "z")

            // Greek letters used as Latin lookalikes. Λ/λ lists A first because
            // titles use it as a stylized A (ΛZΛLEΛ).
            register("αά", "a")
            register("β", "b")
            register("γ", "gy")
            register("δ", "d")
            register("εέ", "e")
            register("ζ", "z")
            register("ηή", "in")
            register("θ", "o")
            register("ιίϊΐ", "i")
            register("κ", "k")
            register("λ", "al")
            register("μ", "mu")
            register("ν", "nv")
            register("ξ", "x")
            register("οό", "o")
            register("π", "pn")
            register("ρ", "rp")
            register("σς", "s")
            register("τ", "t")
            register("υύϋΰ", "yuv")
            register("φ", "f")
            register("χ", "x")
            register("ψ", "p")
            register("ωώ", "wo")

            // Cyrillic Я used as a reversed R (VECTOЯ).
            register("я", "r")

            // Superscript and subscript digits.
            register("⁰₀", "0")
            register("¹₁", "1")
            register("²₂", "2")
            register("³₃", "3")
            register("⁴₄", "4")
            register("⁵₅", "5")
            register("⁶₆", "6")
            register("⁷₇", "7")
            register("⁸₈", "8")
            register("⁹₉", "9")

            // Symbols typed as ASCII.
            register("×", "x")
            register("〜", "~")
        }

    private val AMBIGUOUS_ALIASES: List<String> = ALIASES.values.filter { it.length > 1 }.distinct()

    private val CLASS_CHAR_BY_ALIASES: Map<String, Char> =
        AMBIGUOUS_ALIASES.withIndex().associate { (index, aliases) -> aliases to ('\uE000' + index) }

    private val ALIASES_BY_CLASS_CHAR: Map<Char, String> =
        AMBIGUOUS_ALIASES.withIndex().associate { (index, aliases) -> ('\uE000' + index) to aliases }

    private val CLASS_CHAR_RANGE: CharRange = '\uE000'..('\uE000' + AMBIGUOUS_ALIASES.size)

    private fun classOf(ch: Char): Char {
        val aliases = ALIASES[ch] ?: return ch
        return if (aliases.length == 1) aliases[0] else CLASS_CHAR_BY_ALIASES.getValue(aliases)
    }

    private fun classAliases(ch: Char): String? = if (ch in CLASS_CHAR_RANGE) ALIASES_BY_CLASS_CHAR[ch] else null

    private fun accepts(
        left: Char,
        right: Char,
    ): Boolean {
        if (left == right) return true

        val leftAliases = classAliases(left)
        val rightAliases = classAliases(right)
        return when {
            leftAliases != null && rightAliases != null -> leftAliases.any { it in rightAliases }
            leftAliases != null -> right in leftAliases
            rightAliases != null -> left in rightAliases
            else -> false
        }
    }

    private fun isWordChar(ch: Char): Boolean = ch.isLetterOrDigit() || classAliases(ch) != null

    fun tokenize(query: String): List<String> = normalize(query).split(TOKEN_SEPARATOR).filter { it.isNotEmpty() }

    private fun tokenizeClasses(query: String): List<String> = encodeClasses(query).split(' ').filter { it.isNotEmpty() }

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
        val tokens = tokenizeClasses(query)
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
        val tokens = tokenizeClasses(query)
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
        if (token.isEmpty() || text.isEmpty() || token.length > text.length) return null

        val lastStart = text.length - token.length
        var substringIndex = -1
        var start = 0
        while (start <= lastStart) {
            if (matchesAt(token, text, start)) {
                if (start == 0) {
                    val quality = if (text.length == token.length) QUALITY_EXACT else QUALITY_PREFIX
                    return TokenMatch(quality, 0)
                }
                if (substringIndex < 0) substringIndex = start
                if (!isWordChar(text[start - 1])) return TokenMatch(QUALITY_WORD_PREFIX, start)
            }
            start++
        }

        return if (substringIndex >= 0) TokenMatch(QUALITY_SUBSTRING, substringIndex) else null
    }

    private fun matchesAt(
        token: String,
        text: String,
        start: Int,
    ): Boolean {
        for (offset in token.indices) {
            if (!accepts(text[start + offset], token[offset])) return false
        }
        return true
    }
}
