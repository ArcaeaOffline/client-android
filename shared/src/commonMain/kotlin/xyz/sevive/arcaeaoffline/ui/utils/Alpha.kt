package xyz.sevive.arcaeaoffline.ui.utils

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha

const val DISABLED_ALPHA = .38f
const val SECONDARY_ALPHA = .75f

fun Modifier.disabledItemAlpha() = this.alpha(DISABLED_ALPHA)

fun Modifier.secondaryItemAlpha() = this.alpha(SECONDARY_ALPHA)
