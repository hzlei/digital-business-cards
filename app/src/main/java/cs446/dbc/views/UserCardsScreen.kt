package cs446.dbc.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
<<<<<<< HEAD
=======
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
>>>>>>> 09c834d (reworked business card view model (state was being shared))
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import cs446.dbc.components.BusinessCard
import cs446.dbc.models.BusinessCardModel
import cs446.dbc.viewmodels.AppViewModel
import cs446.dbc.viewmodels.BusinessCardAction
import cs446.dbc.viewmodels.BusinessCardViewModel

@Composable
fun UserCardsScreen(appViewModel: AppViewModel, origCardList: List<BusinessCardModel>) {
    appViewModel.updateScreenTitle("My Cards")
<<<<<<< HEAD
    LazyColumn(
=======
    val cardViewModel: BusinessCardViewModel = viewModel() {
        BusinessCardViewModel(savedStateHandle = createSavedStateHandle())
    }

    // TODO: Remove after, we're just temporarily add cards to mock them for the demo
    for (card in origCardList) {
        cardViewModel.performAction(BusinessCardAction.PopulateCard(
            front = card.front,
            back = card.back,
            favorite = card.favorite,
            fields = card.fields
        ))
    }

    val cards by cardViewModel.businessCard.collectAsStateWithLifecycle()


    Column(
>>>>>>> 09c834d (reworked business card view model (state was being shared))
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
<<<<<<< HEAD
        items(cardList) { card ->
            Box(modifier = Modifier.fillMaxWidth()) {
                BusinessCard(null)
=======
        LazyColumn(
        ) {
            items(cards) { card ->
                Box(modifier = Modifier.fillMaxWidth()) {
                    BusinessCard(card, cardViewModel::performAction)
                }
                Spacer(modifier = Modifier.height(2.dp))
>>>>>>> 09c834d (reworked business card view model (state was being shared))
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
    UserCardsScreen(appViewModel, cardList)
}