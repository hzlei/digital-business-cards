package cs446.dbc.views

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import cs446.dbc.viewmodels.AppViewModel
import cs446.dbc.viewmodels.EventViewModel

@Composable
fun CreateEventScreen(eventViewModel: EventViewModel, appViewModel: AppViewModel, appContext: Context, navController: NavHostController, eventId: String? = null) {







    if (eventId == null) {
        // TODO: create a new event
        // TODO: send cards to event (pick which ones?)
        // TODO: Allow cards to be autoshared
    }
    else {
        // TODO: update event
    }
}