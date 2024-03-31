package cs446.dbc.views

import android.content.Context
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import cs446.dbc.components.BusinessCard
import cs446.dbc.models.BusinessCardModel
import cs446.dbc.models.CardType
import cs446.dbc.viewmodels.AppViewModel
import cs446.dbc.viewmodels.BusinessCardAction
import cs446.dbc.viewmodels.BusinessCardViewModel

@Composable
fun SharedCardsScreen(appViewModel: AppViewModel, sharedCardViewModel: BusinessCardViewModel,
                      origCardList: List<BusinessCardModel>, appContext: Context,
                      navController: NavController)
{
    appViewModel.updateScreenTitle("Shared Cards")
    val sharedCards by sharedCardViewModel.sharedBusinessCards.collectAsStateWithLifecycle()
    val loadedSharedCards by appViewModel.loadedSharedCards.collectAsStateWithLifecycle()
    val composeCards = remember {
        mutableStateListOf<BusinessCardModel>()
    }
    // TODO: Remove after, we're just temporarily add cards to mock them for the demo
    /* TODO: This may work for saved preferences, but it'll be more complicated since we can delete cards
        and do so while switching context to another screen (so we can't just check if the
        businessCards list is empty)
     */


    // First load the cards
    LaunchedEffect(key1 = "load_cards") {
        if (!loadedSharedCards) {
            val cardList =
                appViewModel.loadCardsFromDirectory(appContext, "businessCards", CardType.SHARED)
            sharedCardViewModel.performAction(BusinessCardAction.InsertCards(cardList))
        }
    }

    // TODO: Remove this later, we add examples
    LaunchedEffect(key1 = Unit) {
        if (sharedCards.isEmpty()) {
            origCardList.forEach { card ->
//                appViewModel.addCard(card, appContext, "businessCards", CardType.SHARED)
                sharedCardViewModel.performAction(BusinessCardAction.InsertCard(card))
            }
        }
    }

    sharedCardViewModel.businssCardSnapshotList = composeCards

    LaunchedEffect(key1 = "load_examples") {
        if (sharedCards.size < 1) {
            origCardList.forEach { card ->
                sharedCardViewModel.performAction(
                    BusinessCardAction.PopulateCard(
                        front = card.front,
                        back = card.back,
                        favorite = card.favorite,
                        fields = card.fields,
                        cardType = card.cardType
                    )
                )
            }
        }
    }

    if (composeCards.isEmpty()) {
        composeCards.addAll(sharedCards)
    }

    LazyColumn(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        items(composeCards) { card ->
            Box(modifier = Modifier.fillMaxWidth()) {
                BusinessCard(card, true, navController, sharedCardViewModel::performAction)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun SharedCardsScreenPreview() {
    val appContext = LocalContext.current
    val appViewModel: AppViewModel = viewModel()
    val cardList: List<BusinessCardModel> = listOf()
    val cardViewModel: BusinessCardViewModel = viewModel() {
        BusinessCardViewModel(savedStateHandle = createSavedStateHandle(), CardType.SHARED)
    }
    val navController = NavHostController(appContext)
    SharedCardsScreen(appViewModel, cardViewModel, cardList, appContext, navController)
}