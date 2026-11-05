package xyz.sevive.arcaeaoffline.ui.helpers

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

/** A one-shot [UiEvent] queue for a ViewModel. */
class UiEventChannel {
    private val channel = Channel<UiEvent>(Channel.BUFFERED)

    val events: Flow<UiEvent> = channel.receiveAsFlow()

    suspend fun send(event: UiEvent) = channel.send(event)
}
