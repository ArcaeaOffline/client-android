package xyz.sevive.arcaeaoffline.ui.models

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import xyz.sevive.arcaeaoffline.helpers.OcrDependencyHelper

class OcrDependencyViewModel : ViewModel() {
    val knnModelState = OcrDependencyHelper.kNearestModel.combine(
        OcrDependencyHelper.kNearestModelException
    ) { model, exception -> KnnModelState(model = model, error = exception) }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), KnnModelState()
    )

    val phashDatabaseState = OcrDependencyHelper.imagePhashDatabase.combine(
        OcrDependencyHelper.imagePhashDatabaseException
    ) { db, exception -> PhashDatabaseState(db = db, error = exception) }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), PhashDatabaseState()
    )

    fun reload(context: Context) {
        OcrDependencyHelper.loadAll(context)
    }

    companion object {
        const val TIMEOUT_MILLIS = 2500L
    }
}
