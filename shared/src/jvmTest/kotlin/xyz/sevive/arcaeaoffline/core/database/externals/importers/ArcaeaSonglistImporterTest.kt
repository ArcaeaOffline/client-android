package xyz.sevive.arcaeaoffline.core.database.externals.importers

import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ArcaeaSonglistImporterTest {
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
