package cs446.dbc.views

import android.app.Application
import android.content.Context
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
fun UserCardsScreen(appViewModel: AppViewModel, myCardViewModel: BusinessCardViewModel,
                    origCardList: List<BusinessCardModel>, appContext: Context,
                    navController: NavController)
{
    appViewModel.updateScreenTitle("My Cards")
    val cards by myCardViewModel.myBusinessCards.collectAsStateWithLifecycle()
    val loadedMyCards by appViewModel.loadedMyCards.collectAsStateWithLifecycle()

    val composeCards = remember {
        mutableStateListOf<BusinessCardModel>()
    }

    myCardViewModel.updateCardContext("myBusinessCards")
    myCardViewModel.businssCardSnapshotList = composeCards

    LaunchedEffect(key1 = "load_cards") {
        if (!loadedMyCards) {
            val cardList =
                appViewModel.loadCardsFromDirectory(appContext, "businessCards", CardType.PERSONAL)
            myCardViewModel.performAction(BusinessCardAction.InsertCards(cardList))
        }
    }

    // TODO: Remove after, we're just temporarily add cards to mock them for the demo
    /* TODO: This may work for saved preferences, but it'll be more complicated since we can delete cards
        and do so while switching context to another screen (so we can't just check if the
        businessCards list is empty)
     */


//    LaunchedEffect(key1 = Unit) {
//        if (cards.isEmpty()) {
//            origCardList.forEach { card ->
//                appViewModel.addCard(card, appContext, "businessCards", CardType.PERSONAL)
//                myCardViewModel.performAction(BusinessCardAction.InsertCard(card))
//            }
//        }
//    }

    if (composeCards.isEmpty()) {
        composeCards.addAll(cards)
    }

    LazyColumn(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        items(composeCards) { card ->
            Box(modifier = Modifier.fillMaxWidth()) {
                BusinessCard(card, true, navController ,myCardViewModel::performAction)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun UserCardsScreenPreview() {
    val appViewModel: AppViewModel = viewModel()
    val cardList: List<BusinessCardModel> = listOf()
    val appContext = LocalContext.current
    val cardViewModel: BusinessCardViewModel = viewModel() {
        BusinessCardViewModel(appContext.applicationContext as Application, savedStateHandle = createSavedStateHandle(), CardType.PERSONAL)
    }
    val navController: NavHostController = NavHostController(appContext)
    UserCardsScreen(appViewModel, cardViewModel, cardList, appContext, navController)
}