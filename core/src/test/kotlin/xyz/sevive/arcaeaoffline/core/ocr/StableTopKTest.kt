package xyz.sevive.arcaeaoffline.core.ocr

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.random.Random

class StableTopKTest {
    @Test
    fun matchesStableSortTruncatedToLimit() {
        val random = Random(20261112)
        repeat(100) {
            val items =
                List(random.nextInt(0, 100)) { index ->
                    index to random.nextInt(0, 5)
                }
            val expected =
                items
                    .sortedBy { it.second }
                    .take(10)
            val actual = stableTopKBy(items.asSequence(), 10) { it.second }.map { it.first }
            assertEquals(expected, actual)
        }
    }

    @Test
    fun equalScoresKeepOriginalOrder() {
        val items = List(50) { it to 7 }
        val actual = stableTopKBy(items.asSequence(), 10) { it.second }.map { it.first }
        assertEquals(items.take(10), actual)
    }

    @Test
    fun fewerItemsThanLimitReturnsAllSorted() {
        val items = listOf(3 to 30, 1 to 10, 2 to 20)
        val actual = stableTopKBy(items.asSequence(), 10) { it.second }.map { it.first }
        assertEquals(listOf(1 to 10, 2 to 20, 3 to 30), actual)
    }
}
