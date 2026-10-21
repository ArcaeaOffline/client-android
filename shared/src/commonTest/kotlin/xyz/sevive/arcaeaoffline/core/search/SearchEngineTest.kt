package xyz.sevive.arcaeaoffline.core.search

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SearchEngineTest {
    private fun document(
        id: String,
        title: String,
        artist: String = "",
        pack: String = "",
    ): SearchDocument =
        searchDocument(id) {
            text(SearchFields.Title, title)
            text(SearchFields.Artist, artist)
            text(SearchFields.PackName, pack)
        }

    @Test
    fun normalizeFoldsWidthCaseAndSpaces() {
        assertEquals("nova field", SearchEngine.normalize("ＮＯＶＡ　Ｆｉｅｌｄ"))
        assertEquals("mixed case", SearchEngine.normalize("  Mixed CASE  "))
        assertEquals("", SearchEngine.normalize("　 "))
    }

    @Test
    fun foldsAccentsGreekCyrillicAndSymbols() {
        assertEquals("avril flicka i krans", SearchEngine.normalize("Ävril Flicka i krans"))
        assertEquals("azalea vector odysseia", SearchEngine.normalize("ΛZΛLEΛ VECTOЯ ΟΔΥΣΣΕΙΑ"))
        assertEquals("w4", SearchEngine.normalize("ω4"))
        assertEquals("2 0 x ~", SearchEngine.normalize("² ₀ × 〜"))
    }

    @Test
    fun matchesFoldedLettersAndSymbols() {
        val documents =
            listOf(
                document("a", "Ävril", artist = "VECTOЯ"),
                document("b", "Track", artist = "ak+q × onoken"),
            )

        assertEquals(listOf("a"), SearchEngine.search(documents, "avril vector").map { it.documentId })
        assertEquals(listOf("b"), SearchEngine.search(documents, "onoken x").map { it.documentId })
    }

    @Test
    fun tokenizeSplitsOnWhitespace() {
        assertEquals(listOf("nova", "field"), SearchEngine.tokenize("  Nova \t Field "))
        assertTrue(SearchEngine.tokenize("   ").isEmpty())
    }

    @Test
    fun matchesSubstringAndReportsHighlight() {
        val doc = document("a", "Nova Field")

        val match = SearchEngine.search(listOf(doc), "field").single()

        assertEquals("a", match.documentId)
        assertEquals(listOf(5..9), match.highlights[SearchFields.Title.key])
    }

    @Test
    fun requiresEveryTokenAcrossFields() {
        val documents =
            listOf(
                document("a", "Nova Field", artist = "Composer.Name"),
                document("b", "Nova Star", artist = "other"),
            )

        assertEquals(
            listOf("a"),
            SearchEngine.search(documents, "nova composer").map { it.documentId },
        )
        assertTrue(SearchEngine.search(documents, "nova composer missing").isEmpty())
    }

    @Test
    fun ranksExactThenPrefixThenWordPrefix() {
        val documents =
            listOf(
                document("word", "The Nova"),
                document("prefix-long", "Nova Field"),
                document("prefix-short", "NovaX"),
                document("exact", "Nova"),
            )

        assertEquals(
            listOf("exact", "prefix-short", "prefix-long", "word"),
            SearchEngine.search(documents, "nova").map { it.documentId },
        )
    }

    @Test
    fun titleOutranksPackName() {
        val documents =
            listOf(
                document("pack", "Other", pack = "Nova Pack"),
                document("title", "Nova", pack = "Test Archive"),
            )

        assertEquals(
            listOf("title", "pack"),
            SearchEngine.search(documents, "nova").map { it.documentId },
        )
    }

    @Test
    fun highlightsArtistWhenArtistMatches() {
        val match = SearchEngine.search(listOf(document("a", "Track", artist = "Composer.Name")), "composer").single()

        assertEquals(listOf(0..7), match.highlights[SearchFields.Artist.key])
        assertNull(match.highlights[SearchFields.Title.key])
    }

    @Test
    fun limitCapsRankedResults() {
        val documents = (1..5).map { document("s$it", "Track $it") }

        assertEquals(2, SearchEngine.search(documents, "track", limit = 2).size)
        assertEquals(5, SearchEngine.search(documents, "track").size)
    }

    @Test
    fun searchIdsReturnsOnlyMatches() {
        val documents =
            listOf(
                document("a", "Nova Field"),
                document("b", "Silent Harbor"),
                document("c", "Nova Star"),
            )

        assertEquals(setOf("a", "c"), SearchEngine.searchIds(documents, "nova"))
        assertTrue(SearchEngine.searchIds(documents, "zzz").isEmpty())
    }

    @Test
    fun blankQueryMatchesNothing() {
        val documents = listOf(document("a", "Nova Field"))

        assertTrue(SearchEngine.search(documents, "   ").isEmpty())
        assertTrue(SearchEngine.searchIds(documents, "").isEmpty())
    }

    @Test
    fun fullWidthQueryMatchesAsciiTitle() {
        val documents = listOf(document("a", "Nova Field"))

        assertEquals(listOf("a"), SearchEngine.search(documents, "ＮＯＶＡ").map { it.documentId })
    }

    @Test
    fun documentBuilderDropsBlanksAndDuplicates() {
        val built =
            searchDocument("a") {
                text(SearchFields.Title, "Nova Field")
                text(SearchFields.Title, "nova field")
                text(SearchFields.Title, "   ")
                text(SearchFields.PackName, null)
            }

        assertEquals(1, built.fields.size)
    }

    @Test
    fun normalizeKeepsFirstReadingAsCanonical() {
        assertEquals("g", SearchEngine.normalize("γ"))
        assertEquals("y", SearchEngine.normalize("υ"))
        assertEquals("a", SearchEngine.normalize("λ"))
    }

    @Test
    fun matchesEveryReadingOfGreekLookalikes() {
        val documents =
            listOf(
                document("gamma", "γ"),
                document("upsilon", "υ"),
            )

        assertEquals(setOf("gamma"), SearchEngine.search(documents, "g").map { it.documentId }.toSet())
        assertEquals(setOf("gamma", "upsilon"), SearchEngine.search(documents, "y").map { it.documentId }.toSet())
        assertEquals(setOf("upsilon"), SearchEngine.search(documents, "u").map { it.documentId }.toSet())
        assertEquals(setOf("upsilon"), SearchEngine.search(documents, "v").map { it.documentId }.toSet())
    }

    @Test
    fun matchesMixedGreekReadingsWithinOneWord() {
        val documents = listOf(document("pair", "γυ"))

        assertEquals(listOf("pair"), SearchEngine.search(documents, "gu").map { it.documentId })
        assertEquals(listOf("pair"), SearchEngine.search(documents, "gy").map { it.documentId })
        assertEquals(listOf("pair"), SearchEngine.search(documents, "yv").map { it.documentId })
    }

    @Test
    fun highlightsGreekMatchesAtOriginalPositions() {
        val match = SearchEngine.search(listOf(document("pair", "γυ")), "u").single()

        assertEquals(listOf(1..1), match.highlights[SearchFields.Title.key])
    }

    @Test
    fun greekQueryMatchesLatinText() {
        val documents = listOf(document("a", "odysseia"))

        assertEquals(listOf("a"), SearchEngine.search(documents, "οδυσσεια").map { it.documentId })
    }

    @Test
    fun doesNotCrossMatchUnrelatedLatinLetters() {
        assertTrue(SearchEngine.search(listOf(document("a", "u")), "v").isEmpty())
        assertTrue(SearchEngine.search(listOf(document("a", "g")), "y").isEmpty())
    }

    @Test
    fun matchesLambdaAsEitherReading() {
        val documents = listOf(document("lambda", "λ"))

        assertEquals(listOf("lambda"), SearchEngine.search(documents, "a").map { it.documentId })
        assertEquals(listOf("lambda"), SearchEngine.search(documents, "l").map { it.documentId })
    }

    @Test
    fun matchesCompletedGreekTable() {
        val readings =
            mapOf(
                "η" to "in",
                "ν" to "nv",
                "ρ" to "rp",
                "π" to "pn",
                "μ" to "mu",
                "ω" to "wo",
                "θ" to "o",
                "φ" to "f",
                "ψ" to "p",
                "ξ" to "x",
                "ζ" to "z",
            )

        for ((letter, aliases) in readings) {
            val documents = listOf(document(letter, letter))

            for (alias in aliases) {
                assertEquals(listOf(letter), SearchEngine.search(documents, alias.toString()).map { it.documentId })
            }
        }
    }

    @Test
    fun chartOverridesHighlightUnderTheirOwnKey() {
        val document =
            searchDocument("a") {
                text(SearchFields.Title, "Nova")
                text(SearchFields.ChartTitle, "Silent Harbor")
            }

        val overrideMatch = SearchEngine.search(listOf(document), "silent").single()
        assertEquals(listOf(0..5), overrideMatch.highlights[SearchFields.ChartTitle.key])
        assertNull(overrideMatch.highlights[SearchFields.Title.key])

        val titleMatch = SearchEngine.search(listOf(document), "nova").single()
        assertEquals(listOf(0..3), titleMatch.highlights[SearchFields.Title.key])
        assertNull(titleMatch.highlights[SearchFields.ChartTitle.key])
    }
}
