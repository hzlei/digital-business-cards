package cs446.dbc.components

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cs446.dbc.models.BusinessCardModel
import cs446.dbc.viewmodels.BusinessCardViewModel
import cs446.dbc.viewmodels.CreateEditViewModel

@Composable
fun BusinessCardMultiSelect(
    title: String,
    cardViewModel: BusinessCardViewModel,
    createEditViewModel: CreateEditViewModel
) {
    val myCards by cardViewModel.myBusinessCards.collectAsStateWithLifecycle()
    val eventBusinessCardList by createEditViewModel.eventBusinessCardList.collectAsStateWithLifecycle()

    // Utilize the SnapshotStateList from the ViewModel directly for reactive UI updates
    var launchedBefore by rememberSaveable { mutableStateOf(false) }

    // Initialize or update the items list based on the myCards state.
    var items by rememberSaveable { mutableStateOf(myCards.map { BusinessCardModel(it.id, it.front, it.back, it.favorite, it.fields, it.template, it.cardType, it.eventId, it.eventUserId, isSelected = eventBusinessCardList.any { selectedCard -> selectedCard.id == it.id }) }) }

    // Effect to initialize or reset the selection state when the component is first launched or when the myCards list changes.
    LaunchedEffect(key1 = myCards) {
        if (!launchedBefore || myCards.isNotEmpty()) {
            items = myCards.map { card ->
                card.copy(isSelected = eventBusinessCardList.any { selectedCard -> selectedCard.id == card.id })
            }
            launchedBefore = true
        }
    }

    Column {
        Text(
            text = title,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(items) { card ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            // Toggle selection state on click
                            card.isSelected = !card.isSelected
                            // Update the eventBusinessCardList in the ViewModel based on the new selection state
                            if (card.isSelected) {
                                createEditViewModel.addCardToEvent(card)
                            } else {
                                createEditViewModel.removeCardFromEvent(card.id)
                            }
                        }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Card(modifier = Modifier.weight(0.75f)) {
                        BusinessCard(card, isEnabled = false) { action ->
                            // Handle any card-specific actions here, if necessary
                        }
                    }
                    Box(
                        modifier = Modifier.weight(0.25f),
                        contentAlignment = Alignment.Center
                    ) {
                        if (card.isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Card Selected",
                                tint = Color.Green
                            )
                        }
                    }
                }
            }
        }
    }
}
