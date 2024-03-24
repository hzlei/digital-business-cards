package cs446.dbc.views

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import cs446.dbc.MainActivity
import cs446.dbc.models.EventModel
import cs446.dbc.viewmodels.AppViewModel
import cs446.dbc.viewmodels.EventViewModel

@Composable
fun EventMenuScreen(eventViewModel: EventViewModel, appViewModel: AppViewModel, appContext: Context, navController: NavHostController, eventId: String?) {
    val events by eventViewModel.events.collectAsStateWithLifecycle()
    var currEvent = events.find { event -> event.id == eventId }
    if (eventId == null || currEvent == null) {
        Toast.makeText(appContext, "ERROR: That event does not exist!", Toast.LENGTH_LONG).show()
        navController.popBackStack()
    }
    appViewModel.updateScreenTitle("Event: ${currEvent?.name}")
    Box {

    }
}