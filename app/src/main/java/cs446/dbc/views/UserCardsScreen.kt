package cs446.dbc.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import cs446.dbc.components.BusinessCard
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cs446.dbc.models.BusinessCardModel
import cs446.dbc.models.Field
import cs446.dbc.models.FieldType
import cs446.dbc.viewmodels.AppViewModel


@Composable
fun UserCardsScreen(appViewModel: AppViewModel) {
    val cardList by appViewModel.cards.collectAsState()
    val context = LocalContext.current

    val directoryName = "businessCards"

    LaunchedEffect(key1 = true) {
        appViewModel.loadCardsFromDirectory(context, directoryName)
    }

    appViewModel.updateScreenTitle("My Cards")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.weight(0.95f)
        ) {
            LazyColumn {
                items(cardList) { card ->
                    // Now passing the actual card object to BusinessCard composable
                    Box(modifier = Modifier.fillMaxWidth()) {
                        BusinessCard(card = card)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.weight(0.05f),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Spacer(modifier = Modifier.fillMaxHeight())
            FloatingActionButton(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(),
                onClick = {
                    // REPLACE THIS WITH THE ACTUAL BUSINESS CARD CREATION UI
                    val exampleCard = BusinessCardModel(
                        front = "Example Front",
                        back = "Example Back",
                        favorite = false,
                        fields = mutableListOf(
                            Field("Example Field", "Example Value", FieldType.TEXT)
                        )
                    )
                    appViewModel.saveCardToLocalStorage(exampleCard, context, directoryName)
                    appViewModel.addCard(exampleCard)
                }
            ) {
                Icon(
                    imageVector = Icons.Outlined.AddCircleOutline,
                    contentDescription = "Add Cards"
                )
            }
            Spacer(modifier = Modifier.fillMaxHeight())
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun UserCardsScreenPreview() {
    val appViewModel: AppViewModel = viewModel()
    UserCardsScreen(appViewModel)
}
