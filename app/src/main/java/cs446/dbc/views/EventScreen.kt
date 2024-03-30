package cs446.dbc.views

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import cs446.dbc.components.EventCard
import cs446.dbc.models.EventType
import cs446.dbc.viewmodels.AppViewModel
import cs446.dbc.viewmodels.EventAction
import cs446.dbc.viewmodels.EventViewModel

@Composable
fun EventScreen(eventViewModel: EventViewModel, appViewModel: AppViewModel, appContext: Context, navController: NavHostController) {

    appViewModel.updateScreenTitle("Events")
    val events by eventViewModel.events.collectAsStateWithLifecycle()
//    val loadedEvents by appViewModel.loadedEvents.collectAsStateWithLifecycle()

    // First load the events
//    LaunchedEffect(key1 = "load_events") {
//        if (!loadedEvents) {
//            // TODO: Convert into loading events from storage
//            //val cardList =
//            //    appViewModel.loadCardsFromDirectory(appContext, "businessCards", CardType.SHARED)
//            //sharedCardViewModel.performAction(BusinessCardAction.InsertCards(cardList))
//        }
//    }

    LaunchedEffect(key1 = "joined_event_examples") {
        if (events.isEmpty()) {
            for (i in 0..5) {
                eventViewModel.performAction(EventAction.PopulateEvent(
                    name = "Joined E$i",
                    location = "Toronto - $i",
                    eventType = EventType.JOINED
                ))
            }
            for (i in 0..5) {
                eventViewModel.performAction(EventAction.PopulateEvent(
                    name = "Hosted E$i",
                    location = "Toronto - $i",
                    eventType = EventType.HOSTED
                ))
            }
            eventViewModel.performAction(EventAction.SortEvents())
        }
    }

    LazyColumn (
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        items(events) { event ->
            Box(modifier = Modifier.fillMaxWidth()) {
                EventCard(event, eventViewModel::performAction, onClickAction = {
                    // TODO: Change this to allow for API <= Ver 32
                    navController.navigate(route = "event-menu/${event.id}"
                    )}
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}