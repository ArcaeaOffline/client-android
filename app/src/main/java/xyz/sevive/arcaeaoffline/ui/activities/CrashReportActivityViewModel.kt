package xyz.sevive.arcaeaoffline.ui.activities

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow


class CrashReportViewModel : ViewModel() {
    private val _comment = MutableStateFlow<String?>(null)
    val comment = _comment.asStateFlow()
    private val _contact = MutableStateFlow<String?>(null)
    val contact = _contact.asStateFlow()

    fun setComment(comment: String?) {
        _comment.value = comment
    }

    fun setContact(contact: String?) {
        _contact.value = contact
    }

    private val _reportProcessed = MutableStateFlow(false)
    val reportProcessed = _reportProcessed.asStateFlow()

    fun reportProcessed() {
        _reportProcessed.value = true
    }
}
