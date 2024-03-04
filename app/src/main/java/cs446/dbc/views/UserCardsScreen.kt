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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cs446.dbc.components.BusinessCard
import cs446.dbc.models.BusinessCardModel
import cs446.dbc.viewmodels.AppViewModel

@Composable
fun UserCardsScreen(appViewModel: AppViewModel, cardList: List<BusinessCardModel>) {
    appViewModel.updateScreenTitle("My Cards")
    LazyColumn(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        items(cardList) { card ->
            Box(modifier = Modifier.fillMaxWidth()) {
                BusinessCard(null)
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