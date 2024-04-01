package cs446.dbc.views

import android.content.Context
import android.widget.Toast
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
import cs446.dbc.api.ApiFunctions
import cs446.dbc.components.BusinessCard
import cs446.dbc.models.BusinessCardModel
import cs446.dbc.models.CardType
import cs446.dbc.models.EventModel
import cs446.dbc.models.Field
import cs446.dbc.models.FieldType
import cs446.dbc.models.TemplateType
import cs446.dbc.viewmodels.AppViewModel
import cs446.dbc.viewmodels.BusinessCardViewModel
import cs446.dbc.viewmodels.EventViewModel
import kotlinx.coroutines.runBlocking
import java.util.Random
import java.util.UUID

@Composable
// TODO: rename this to something more appropriate for it's purpose
fun EventMenuScreen(eventViewModel: EventViewModel, appViewModel: AppViewModel, appContext: Context, navController: NavHostController, eventId: String) {
    val events by eventViewModel.events.collectAsStateWithLifecycle()
    val currEvent = events.find { event -> event.id == eventId }

    appViewModel.updateScreenTitle("Event: ${currEvent?.name}")

    LaunchedEffect("checkNonExistentEvent") {
        if (eventId == "" || currEvent == null) {
            Toast.makeText(appContext, "ERROR: That event does not exist!", Toast.LENGTH_LONG).show()
            navController.popBackStack()
        }
    }

    val eventBusinessCardList: MutableList<BusinessCardModel> = mutableListOf<BusinessCardModel>()
    runBlocking {
        val serverCardList = ApiFunctions.getAllEventCards(eventId)
        eventBusinessCardList.addAll(serverCardList)
    }

    // Set event id for current event in view model

    val eventBusinessCardViewModel: BusinessCardViewModel = viewModel() {
        BusinessCardViewModel(savedStateHandle = createSavedStateHandle(), CardType.SHARED)
    }


    // TODO: These cards should all have a card type of shared!
    // TODO: how are we adding the toolbar under each card to request for the card?
    // TODO: how are we allowing the user to see more information about the card?
    // e.g. name, company name, industry, etc.?
    // TODO: ^ Maybe force use the EventViewTemplate from the BusinessCardFaceTemplate section
    //  probably by altering the template type
    //  though make sure we preserve their background and stuff


//    // TODO: Remove these example ones later, they're only to test UI
//    for (i in 0..7) {
//        eventBusinessCardList.add(
//            BusinessCardModel(
//                id = UUID.randomUUID().toString(),
//                front = "CARD ${i + 1}",
//                back = "CARD ${i + 1}",
//                favorite = false,
//                fields = mutableListOf<Field>(
//                    Field(
//                        name = "Full Name",
//                        value = "First Last $i",
//                        type = FieldType.TEXT
//                    ),
//                    Field(
//                        name = "Phone number",
//                        value = "${(1..10).map { (0..9).random() }}",
//                        type = FieldType.PHONE_NUMBER
//                    ),
//                    Field(
//                        name = "Organization",
//                        value = "Test Org $i",
//                        type = FieldType.TEXT
//                    ),
//                ),
//                cardType = CardType.EVENT_VIEW
//            )
//        )
//    }

//    eventBusinessCardList.forEach {card ->
//        card.template = TemplateType.EVENT_VIEW_TEMPLATE
//    }
    // TODO: Add swipe refresh https://www.youtube.com/watch?v=lHdhqk8Bft0
    Box {
        LazyColumn (
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
        ) {
            items(eventBusinessCardList) {card ->
                // TODO: We may need to wrap the cards around with a box and add a toolbar underneath
                Box(modifier = Modifier.fillMaxWidth()) {
                    BusinessCard(card, true, eventBusinessCardViewModel::performAction)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}