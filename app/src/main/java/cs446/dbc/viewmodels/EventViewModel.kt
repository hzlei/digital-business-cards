package cs446.dbc.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import cs446.dbc.models.EventModel
import java.util.UUID

@HiltViewModel
class EventViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
): ViewModel() {

    // TODO: How will we handle injection of saved preferences??
    val events = savedStateHandle.getStateFlow("events", mutableListOf<EventModel>())

    // TODO: Do we need a separate remove card action when removing the card?
    fun performAction(action: EventAction) {
        when (action) {
            is EventAction.PopulateEvent -> populateEvent(action)
            is EventAction.InsertEvent -> TODO()
            is EventAction.InsertEvents -> TODO()
            is EventAction.RemoveEvent -> TODO()
            is EventAction.UpdateEvent -> TODO()
            is EventAction.SortEvents -> sortEvents(action.compareBy)
        }
    }

    private fun populateEvent(action: EventAction.PopulateEvent) {
        // Create temp event
        val event: EventModel = EventModel(
            id = UUID.randomUUID().toString(),
            name = action.name,
            location = action.location,
            eventType = action.eventType,
            numUsers = 0,
            maxUsers = action.maxUsers
        )
        // TODO: Send event creation to server
        // TODO: retrieve event id from server
        // TODO: update event id in event
        val events = savedStateHandle.get<MutableList<EventModel>>("events")
        events?.add(event)
        savedStateHandle["events"] = events
        sortEvents()
    }

    private fun sortEvents(comparator: Comparator<EventModel> = compareBy<EventModel> { it.eventType }) {
        val events = savedStateHandle.get<MutableList<EventModel>>("events")
        events?.sortWith( comparator )
        savedStateHandle["events"] = events

    }
}