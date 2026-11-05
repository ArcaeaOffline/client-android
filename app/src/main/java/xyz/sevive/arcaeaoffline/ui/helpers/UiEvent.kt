package xyz.sevive.arcaeaoffline.ui.helpers

import android.content.res.Resources

/**
 * A one-shot UI message emitted by a ViewModel.
 *
 * TODO: combine with ImportLog?
 */
sealed class UiEvent {
    data class StringRes(
        @androidx.annotation.StringRes val resId: Int,
        val formatArgs: List<Any> = emptyList(),
    ) : UiEvent()

    data class PluralRes(
        @androidx.annotation.PluralsRes val resId: Int,
        val quantity: Int,
        val formatArgs: List<Any> = emptyList(),
    ) : UiEvent()

    fun asString(resources: Resources): String =
        when (this) {
            is StringRes -> resources.getString(resId, *formatArgs.toTypedArray())
            is PluralRes -> resources.getQuantityString(resId, quantity, *formatArgs.toTypedArray())
        }
}
