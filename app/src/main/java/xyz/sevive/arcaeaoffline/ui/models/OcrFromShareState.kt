package xyz.sevive.arcaeaoffline.ui.models

import xyz.sevive.arcaeaoffline.core.database.entities.Score

data class OcrFromShareState(val score: Score? = null, val error: Exception? = null)
