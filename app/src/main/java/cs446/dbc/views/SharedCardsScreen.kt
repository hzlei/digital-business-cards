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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import cs446.dbc.components.BusinessCard
import cs446.dbc.models.BusinessCardModel
import cs446.dbc.models.CardType
import cs446.dbc.viewmodels.AppViewModel
import cs446.dbc.viewmodels.BusinessCardAction
import cs446.dbc.viewmodels.BusinessCardViewModel

@Composable
fun SharedCardsScreen(appViewModel: AppViewModel, sharedCardViewModel: BusinessCardViewModel, origCardList: List<BusinessCardModel>) {
    appViewModel.updateScreenTitle("Saved Cards")
    val sharedCards by sharedCardViewModel.sharedBusinessCards.collectAsStateWithLifecycle()
    // TODO: Remove after, we're just temporarily add cards to mock them for the demo
    /* TODO: This may work for saved preferences, but it'll be more complicated since we can delete cards
        and do so while switching context to another screen (so we can't just check if the
        businessCards list is empty)
     */
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

    LazyColumn(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        items(sharedCards) { card ->
            Box(modifier = Modifier.fillMaxWidth()) {
                BusinessCard(card, sharedCardViewModel::performAction)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun SharedCardsScreenPreview() {
    val appViewModel: AppViewModel = viewModel()
    val cardList: List<BusinessCardModel> = listOf()
    val cardViewModel: BusinessCardViewModel = viewModel() {
        BusinessCardViewModel(savedStateHandle = createSavedStateHandle(), CardType.SHARED)
    }
    UserCardsScreen(appViewModel, cardViewModel, cardList)
}