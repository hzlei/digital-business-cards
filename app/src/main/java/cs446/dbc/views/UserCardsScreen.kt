package cs446.dbc.views

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import cs446.dbc.components.BusinessCard
import cs446.dbc.models.BusinessCardModel
import cs446.dbc.viewmodels.AppViewModel
import cs446.dbc.viewmodels.BusinessCardAction
import cs446.dbc.viewmodels.BusinessCardViewModel

@Composable
fun UserCardsScreen(
    appViewModel: AppViewModel,
    myCardViewModel: BusinessCardViewModel,
    navController: NavHostController // Add NavController parameter to navigate
) {
    appViewModel.updateScreenTitle("My Cards")
    val cards by myCardViewModel.myBusinessCards.collectAsStateWithLifecycle()

    val composeCards = remember { mutableStateListOf<BusinessCardModel>().also { it.addAll(cards) } }

    LaunchedEffect(key1 = "load_cards", block = {
        myCardViewModel.businessCardSnapshotList = composeCards
    })

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("createCard") }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Card")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            items(composeCards) { card ->
                Box(modifier = Modifier.fillMaxWidth()) {
                    BusinessCard(card, true) { action ->
                        // Handle card actions here
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
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
        BusinessCardViewModel(savedStateHandle = createSavedStateHandle(), CardType.PERSONAL)
    }
    UserCardsScreen(appViewModel, cardViewModel, cardList, appContext)
}