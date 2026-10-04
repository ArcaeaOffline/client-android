package xyz.sevive.arcaeaoffline.helpers

import org.junit.Assert.assertEquals
import org.junit.Test
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaScoringMode
import xyz.sevive.arcaeaoffline.core.database.repositories.PotentialGroup
import xyz.sevive.arcaeaoffline.core.database.repositories.PotentialGroups

class PotentialCalculationTest {
    private companion object {
        const val TOLERANCE = 1e-7
    }

    @Test
    fun b50Calculation() {
        val groups =
            PotentialGroups(
                b50 = PotentialGroup(items = emptyList(), total = 60.0, isComplete = true),
                b10 = PotentialGroup(items = emptyList(), total = 12.0, isComplete = true),
                b30 = PotentialGroup(items = emptyList(), total = 999.0, isComplete = true),
                r10 = PotentialGroup(items = emptyList(), total = 999.0, isComplete = true),
            )

        assertEquals(1.2, ArcaeaScoringMode.B50.calculatePotential(groups), TOLERANCE)
    }

    @Test
    fun b30R10Calculation() {
        val groups =
            PotentialGroups(
                b50 = PotentialGroup(items = emptyList(), total = 999.0, isComplete = true),
                b10 = PotentialGroup(items = emptyList(), total = 999.0, isComplete = true),
                b30 = PotentialGroup(items = emptyList(), total = 30.0, isComplete = true),
                r10 = PotentialGroup(items = emptyList(), total = 10.0, isComplete = true),
            )

        assertEquals(1.0, ArcaeaScoringMode.B30_R10.calculatePotential(groups), TOLERANCE)
    }
}
