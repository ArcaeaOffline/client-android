package xyz.sevive.arcaeaoffline.ui.utils

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha

const val SECONDARY_ALPHA = .75f

fun Modifier.secondaryItemAlpha() = this.alpha(SECONDARY_ALPHA)
