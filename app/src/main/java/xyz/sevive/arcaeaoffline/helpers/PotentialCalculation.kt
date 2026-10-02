package xyz.sevive.arcaeaoffline.helpers

import xyz.sevive.arcaeaoffline.core.calculators.calculatePotentialB30R10
import xyz.sevive.arcaeaoffline.core.calculators.calculatePotentialB50
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaScoringMode
import xyz.sevive.arcaeaoffline.core.database.repositories.PotentialGroups

internal fun ArcaeaScoringMode.calculatePotential(potentialGroups: PotentialGroups): Double =
    when (this) {
        ArcaeaScoringMode.B30_R10 -> calculatePotentialB30R10(potentialGroups.b30.total, potentialGroups.r10.total)
        ArcaeaScoringMode.B50 -> calculatePotentialB50(potentialGroups.b50.total, potentialGroups.b10.total)
    }
