package cs446.dbc.viewmodels

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import cs446.dbc.models.EventModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Date
import java.util.UUID
import java.util.function.Predicate

@HiltViewModel
class EventViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
): ViewModel() {

    // TODO: How will we handle injection of saved preferences??
    val events = savedStateHandle.getStateFlow("events", mutableListOf<EventModel>())

    private var eventSnapshotList: SnapshotStateList<EventModel>? = null

    val currEventViewId = savedStateHandle.getStateFlow("currEventViewId", "")

    // TODO: Do we need a separate remove card action when removing the card?
    fun performAction(action: EventAction) {
        when (action) {
            is EventAction.PopulateEvent -> populateEvent(action)
            is EventAction.InsertEvent -> TODO()
            is EventAction.InsertEvents -> TODO()
            is EventAction.RemoveEvent -> removeEvent(action)
            is EventAction.UpdateEvent -> TODO()
            is EventAction.SortEvents -> sortEvents(action.compareBy)
            else -> TODO() // not actually, this is just to shut up the error
        }
    }

    private fun populateEvent(action: EventAction.PopulateEvent) {
        // Create temp event
        val event: EventModel = EventModel(
            id = UUID.randomUUID().toString(),
            name = action.name,
            location = action.location,
            startDate = Date().time.toString(),
            endDate = (Date().time + 1000 * 60 * 60 * 24 * 3).toString(),
            numUsers = 0,
            maxUsers = action.maxUsers,
            eventType = action.eventType
        )
        // TODO: Send event creation to server
        // TODO: retrieve event id from server
        // TODO: update event id in event
        val events = savedStateHandle.get<MutableList<EventModel>>("events")
        events?.add(event)
        savedStateHandle["events"] = events
        eventSnapshotList?.add(event)
        sortEvents()
    }

    private fun removeEvent(action: EventAction.RemoveEvent) {
        val event = action.event
        // TODO: Remove event locally
        val events = savedStateHandle.get<MutableList<EventModel>>("events")
        events?.removeIf { it.id == event.id }
        eventSnapshotList?.removeIf(Predicate { it -> it.id == event.id })
        savedStateHandle["events"] = events
        // TODO: Delete from local storage as well

        // TODO: Remove user from event if event is joined

        // TODO: Delete event on server if event is hosted


    }

    private fun sortEvents(comparator: Comparator<EventModel> = compareBy<EventModel> { it.eventType }) {
        val events = savedStateHandle.get<MutableList<EventModel>>("events")
        events?.sortWith( comparator )
        savedStateHandle["events"] = events
        eventSnapshotList?.sortWith(comparator)
    }

    fun changeCurrEventViewId (id: String?) {
        savedStateHandle["currEventViewId"] = id
    }

    fun changeEventSnapshotList (snapshotStateList: SnapshotStateList<EventModel>) {
        eventSnapshotList = snapshotStateList
    }
}