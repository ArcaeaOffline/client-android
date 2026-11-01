package xyz.sevive.arcaeaoffline.core.database.externals.importers

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ArcaeaPacklistImporterTest {
    private val fixture =
        javaClass.classLoader!!
            .getResource("packlist_fixture.json")!!
            .readText()

    @Test
    fun mapsPackFieldsToPack() {
        val packs = ArcaeaPacklistImporter(fixture).packs().associateBy { it.id }

        val base = packs.getValue("testbase")
        assertEquals("Test Base", base.name)
        assertEquals("Base description", base.description)
        assertEquals("collab", base.section)
        assertEquals(101, base.plusCharacterId)
        assertEquals(false, base.isExtendPack)
        assertNull(base.parentPackId)

        val append = packs.getValue("testbase_append_1")
        assertEquals("testbase", append.parentPackId)
        assertNull(append.plusCharacterId)
        assertNull(append.description)
        assertEquals(true, append.isAppendPack)

        val extend = packs.getValue("extend_x")
        assertEquals(true, extend.isExtendPack)
        assertNull(extend.parentPackId)
    }

    @Test
    fun includesBuiltInMemoryArchive() {
        val packs = ArcaeaPacklistImporter(fixture).packs()

        assertEquals(4, packs.size)
        val single = packs.single { it.id == "single" }
        assertEquals("Memory Archive", single.name)
        assertNull(single.parentPackId)
        assertNull(single.section)
    }
}
