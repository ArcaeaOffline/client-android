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
    with(data.keyValueMap) {
        if (isEmpty()) return null
        if (!containsKey(KEY_CURRENT)) return null
        if (!containsKey(KEY_TOTAL)) return null
    }

    return Progress(
        current = data.getInt(KEY_CURRENT, Progress.INDETERMINATE.current),
        total = data.getInt(KEY_TOTAL, Progress.INDETERMINATE.total),
    )
}

fun Progress.Companion.fromWorkInfo(workInfo: WorkInfo?): Progress? =
    when (workInfo?.state) {
        WorkInfo.State.ENQUEUED -> Progress.INDETERMINATE
        WorkInfo.State.RUNNING -> fromWorkData(workInfo.progress)
        else -> null
    }
