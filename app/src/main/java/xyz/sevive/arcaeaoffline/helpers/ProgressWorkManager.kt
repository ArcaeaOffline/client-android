package xyz.sevive.arcaeaoffline.helpers

import androidx.work.Data
import androidx.work.WorkInfo
import xyz.sevive.arcaeaoffline.core.Progress

private const val KEY_CURRENT = "progress_current"
private const val KEY_TOTAL = "progress_total"

fun Progress.toWorkData(): Data =
    Data
        .Builder()
        .putInt(KEY_CURRENT, current)
        .putInt(KEY_TOTAL, total)
        .build()

fun Progress.Companion.fromWorkData(data: Data): Progress? {
    // -1 is used for indeterminate progress
    // so we use -75 here to indicate no value fetched from work data
    val current = data.getInt(KEY_CURRENT, -75)
    val total = data.getInt(KEY_TOTAL, -75)

    return if (current == -75 && total == -75) {
        null
    } else {
        Progress(current = current, total = total)
    }
}

fun Progress.Companion.fromWorkInfo(workInfo: WorkInfo?): Progress? =
    when (workInfo?.state) {
        WorkInfo.State.ENQUEUED -> Progress.INDETERMINATE
        WorkInfo.State.RUNNING -> fromWorkData(workInfo.progress)
        else -> null
    }
