package cs446.dbc.viewmodels

import android.content.Context
import android.util.Log
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cs446.dbc.api.ApiFunctions
import cs446.dbc.models.BusinessCardModel
import cs446.dbc.models.CardType
import cs446.dbc.models.EventModel
import cs446.dbc.models.EventType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.Date
import java.util.UUID
import java.util.function.Predicate
import javax.inject.Inject

@HiltViewModel
class EventViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val context: Context,
    private val userId: String
): ViewModel() {

    // TODO: How will we handle injection of saved preferences??
    val events = savedStateHandle.getStateFlow("events", mutableListOf<EventModel>())

    private var eventSnapshotList: SnapshotStateList<EventModel>? = null

    val currEventViewId = savedStateHandle.getStateFlow("currEventViewId", "")

    // TODO: Do we need a separate remove card action when removing the card?
    fun performAction(action: EventAction) {
        when (action) {
            is EventAction.PopulateEvent -> populateEvent(action)
            is EventAction.InsertEvent -> insertEvent(action)
            is EventAction.InsertEvents -> TODO()
            is EventAction.RemoveEvent -> removeEvent(action)
            is EventAction.UpdateEvent -> updateEvent(action)
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
        saveEventToLocalStorage(event, context, "events")
    }

    private fun insertEvent(action: EventAction.InsertEvent) {
        val event = action.event
        val eventList = savedStateHandle.get<MutableList<EventModel>>("events")
        eventList?.add(event)
        savedStateHandle["events"] = eventList
        eventSnapshotList?.add(event)
        sortEvents()
        saveEventToLocalStorage(event, context, "events")
    }

    private fun updateEvent(action: EventAction.UpdateEvent) {
        val eventList = savedStateHandle.get<MutableList<EventModel>>("events")!!
        eventList.removeIf {
            it.id == action.currEventId
        }
        eventList.add(action.updatedEvent)
        savedStateHandle["events"] = eventList
        eventSnapshotList?.add(action.updatedEvent)
        sortEvents()
        saveEventToLocalStorage(action.updatedEvent, context, "events")
    }

    private fun deleteEventFromLocalStorage(event: EventModel) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val directory = context.getExternalFilesDir("events")

                directory?.let {
                    val fileName = "Event_${event.id}.json"
                    val file = File(it, fileName)

                    file.delete()
                }
            } catch (e: Exception) {
                Log.e("EventViewModel", "Error deleting event from local storage", e)
            }
        }
    }

    private fun removeEvent(action: EventAction.RemoveEvent) {
        val event = action.event
        // TODO: Remove event locally
        val eventList = savedStateHandle.get<MutableList<EventModel>>("events")
        eventList?.removeIf { it.id == event.id }
        eventSnapshotList?.removeIf(Predicate { it -> it.id == event.id })
        savedStateHandle["events"] = eventList
        // Delete from local storage as well
        deleteEventFromLocalStorage(event)
        // Remove user from event if event is joined
        if (event.eventType == EventType.JOINED)  ApiFunctions.exitEvent(event.id, userId)
        // TODO: Delete event on server if event is hosted
        else ApiFunctions.deleteEvent(event.id)
    }

    private fun sortEvents(comparator: Comparator<EventModel> = compareBy<EventModel> (
        { it.eventType }, { it.startDate.toLong() }, { it.endDate.toLong() })
    ) {
        val eventList = savedStateHandle.get<MutableList<EventModel>>("events")
        eventList?.sortWith( comparator )
        savedStateHandle["events"] = eventList
        eventSnapshotList?.sortWith(comparator)
    }

    fun changeCurrEventViewId (id: String?) {
        savedStateHandle["currEventViewId"] = id
    }

    fun changeEventSnapshotList (snapshotStateList: SnapshotStateList<EventModel>) {
        eventSnapshotList = snapshotStateList
    }

    private fun saveEventToLocalStorage(event: EventModel, context: Context, directoryName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val directory = context.getExternalFilesDir(directoryName)

                directory?.let {
                    if (!it.exists()) it.mkdirs()
                    val fileName = "Event_${event.id}.json"
                    val file = File(it, fileName)
                    file.writeText(Json.encodeToString(event))
                }
            } catch (e: Exception) {
                Log.e("AppViewModel", "Error saving event to local storage", e)
            }
        }
    }

    fun loadEventsFromLocalStorage(directoryName: String): MutableList<EventModel> {
        return runBlocking {
            val job = viewModelScope.launch(Dispatchers.IO) {
                val directory = context.getExternalFilesDir(directoryName)
                directory?.let {
                    if (it.exists() && it.isDirectory) {
                        val eventFiles = it.listFiles() ?: return@launch
                        val loadedCards = eventFiles.mapNotNull { file ->
                            try {
                                val eventJson = file.readText()
                                Json.decodeFromString<EventModel>(eventJson)
                            } catch (e: Exception) {
                                Log.e(
                                    "AppViewModel",
                                    "Error reading or parsing event from file: ${file.name}",
                                    e
                                )
                                null
                            }
                        }.toMutableList()

                        savedStateHandle["events"] = loadedCards

                    } else {
                        Log.e("AppViewModel", "Error locating directory: $directoryName")
                    }
                }
            }
            job.join()

            val retEvents =
                savedStateHandle.get<MutableList<EventModel>>("events")
                    ?: mutableListOf<EventModel>()
            retEvents
        }
    }
}