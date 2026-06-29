package xyz.sevive.arcaeaoffline.core

data class Progress(
    val current: Int = 0,
    val total: Int = -1,
) {
    val isIndeterminate: Boolean get() = total < 1

    val fraction: Float
        get() =
            if (isIndeterminate) {
                0f
            } else {
                (current.toFloat() / total).coerceIn(0f, 1f)
            }

    fun increment() = copy(current = (current + 1).coerceAtMost(total))

    companion object {
        val INDETERMINATE = Progress()
    }
}
