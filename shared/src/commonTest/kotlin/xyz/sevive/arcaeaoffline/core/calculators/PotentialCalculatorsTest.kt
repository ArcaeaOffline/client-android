package xyz.sevive.arcaeaoffline.core.calculators

import kotlin.test.Test
import kotlin.test.assertEquals

class PotentialCalculatorsTest {
    companion object {
        const val TOLERANCE: Double = 1e-7
    }

    @Test
    fun testB30R10() {
        assertEquals(
            12.552726250000001,
            calculatePotentialB30R10(377.003555, 125.105495),
            TOLERANCE,
        )
    }

    @Test
    fun testB50() {
        assertEquals(
            12.623817694444446,
            calculatePotentialB50(629.7836316666667, 127.64542999999999),
            TOLERANCE,
        )
    }
}
