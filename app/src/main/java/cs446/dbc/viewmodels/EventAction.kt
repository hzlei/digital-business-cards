package cs446.dbc.viewmodels

import cs446.dbc.models.EventModel
import cs446.dbc.models.EventType

sealed class EventAction {
    data class PopulateEvent (val name: String, val location: String, val eventType: EventType, val maxUsers: Int = Int.MAX_VALUE): EventAction()
    data class InsertEvent (val event: EventModel): EventAction()
    data class InsertEvents (val events: MutableList<EventModel>): EventAction()
    data class RemoveEvent (val event: EventModel): EventAction()
    data class UpdateEvent (val name: String, val location: String, val maxUsers: Int = Int.MAX_VALUE): EventAction()
    data class SortEvents (val compareBy: Comparator<EventModel> = compareBy<EventModel> { it.eventType }): EventAction()
}