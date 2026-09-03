package xyz.sevive.arcaeaoffline.core.database.externals.importers

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass

class ArcaeaSonglistImporterTest {
    // The fixture mirrors upstream alias entries: ratingClassAlias only appears
    // alongside ratingClass 3, and ships with the unknown-to-this-app legacy11
    // key, which must be ignored rather than fail parsing.
    private val fixture =
        javaClass.classLoader!!
            .getResource("songlist_alias_fixture.json")!!
            .readText()

    @Test
    fun ratingClassAliasTest() {
        val difficulties = ArcaeaSonglistImporter(fixture).difficulties()
        assertEquals(2, difficulties.size)

        val present = difficulties.single { it.ratingClass == ArcaeaRatingClass.PRESENT }
        assertNull(present.ratingClassAlias)

        val beyond = difficulties.single { it.ratingClass == ArcaeaRatingClass.BEYOND }
        assertEquals(1, beyond.ratingClassAlias)
    }
}
