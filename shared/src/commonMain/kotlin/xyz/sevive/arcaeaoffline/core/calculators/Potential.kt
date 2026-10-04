package xyz.sevive.arcaeaoffline.core.calculators

fun calculatePotentialB30R10(
    b30Total: Double,
    r10Total: Double,
) = (b30Total + r10Total) / 40.0

fun calculatePotentialB50(
    b50Total: Double,
    b10Total: Double,
) = (b50Total + b10Total) / 60.0
