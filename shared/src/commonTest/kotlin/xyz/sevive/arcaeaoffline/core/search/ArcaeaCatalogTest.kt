package xyz.sevive.arcaeaoffline.core.search

import xyz.sevive.arcaeaoffline.core.database.entities.Pack
import kotlin.test.Test
import kotlin.test.assertEquals

class ArcaeaCatalogTest {
    private val basePack = Pack(id = "nova", name = "Nova Pack")

    private val appendPack = Pack(id = "nova_append_1", name = "Nova Append")

    private val catalog =
        ArcaeaCatalog(
            packs = listOf(basePack, appendPack),
            packsById = mapOf(basePack.id to basePack, appendPack.id to appendPack),
        )

    @Test
    fun packDocumentsIncludeBaseAndAppendNames() {
        assertEquals(setOf("nova"), SearchEngine.searchIds(catalog.packDocuments, "nova pack"))
        assertEquals(setOf("nova"), SearchEngine.searchIds(catalog.packDocuments, "append"))
    }

    @Test
    fun packDocumentsOnlyContainBasePacks() {
        assertEquals(1, catalog.packDocuments.size)
        assertEquals(listOf("nova"), catalog.packDocuments.map { it.id })
    }
}
