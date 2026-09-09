package xyz.sevive.arcaeaoffline.core.api

import kotlin.test.Test
import kotlin.test.assertEquals

class ArcaeaResourcesIndexParsingTest {
    // Example response (2026-09-07)
    private val sampleJson =
        """
        {
          "latest": "7.0.255",
          "versions": [
            {
              "version": "7.0.255",
              "built_at": "2026-09-06T23:46:11+00:00"
            }
          ]
        }
        """.trimIndent()

    @Test
    fun parseRemoteIndexParsesLatestAndBuiltAt() {
        val dto = parseRemoteIndex(sampleJson)

        assertEquals("7.0.255", dto.latest)
        assertEquals(1, dto.versions.size)
        assertEquals("7.0.255", dto.versions[0].version)
        assertEquals("2026-09-06T23:46:11+00:00", dto.versions[0].builtAt)
    }

    @Test
    fun parseRemoteIndexIgnoresUnknownKeys() {
        val dto = parseRemoteIndex("""{"latest": "1.0.0", "future_field": 123}""")

        assertEquals("1.0.0", dto.latest)
    }
}
