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
}
