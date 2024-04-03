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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import cs446.dbc.models.ListItem
import cs446.dbc.viewmodels.BusinessCardViewModel
import cs446.dbc.viewmodels.CreateEditViewModel

@Composable
fun BusinessCardMultiSelect(title: String, cardViewModel: BusinessCardViewModel, createEditViewModel: CreateEditViewModel) {
    val myCards by cardViewModel.myBusinessCards.collectAsStateWithLifecycle()
    val eventBusinessCardList by createEditViewModel.eventBusinessCardList.collectAsStateWithLifecycle()

    var launchedBefore by rememberSaveable {
        mutableStateOf(false)
    }

    var items by rememberSaveable {
        mutableStateOf((0..<myCards.size).map {
            ListItem(card = myCards[it], isSelected = false)
            //ListItem(myCards[it].front, false)
        })
    }

    // TODO: test this with the server to make sure incorrect cards arent being added
    //  or removed, and it's actually a different instance each time we arrive here
    LaunchedEffect (launchedBefore){
        if (!launchedBefore) {
            eventBusinessCardList.clear()
            Log.d("eventBusinessCardList", eventBusinessCardList.size.toString())
            launchedBefore = true
        }
    }

    Column {
        Text(text = title,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .weight(0.1f)
                .fillMaxWidth(),

        )
        LazyColumn(
            modifier = Modifier
                .weight(0.9f)
                .fillMaxSize()
        ) {
            items(items.size) { idx ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            items = items.mapIndexed { i, item ->
                                if (i == idx) {
                                    item.copy(isSelected = !item.isSelected)
                                } else item
                            }
                            items.forEachIndexed { i, item ->
                                if (i == idx) {
                                    if (item.isSelected) {
                                        eventBusinessCardList.add(item.card)
                                    } else {
                                        eventBusinessCardList.remove(item.card)
                                    }
                                }
                            }
                        }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Card(
                        modifier = Modifier.weight(0.75f)
                    ) {
                        BusinessCard(myCards.find { it.id == items[idx].card.id }!!, false, "", NavHostController(
                            LocalContext.current), cardViewModel::performAction)
                        //Text(text = items[idx].title)
                    }
                    Box (
                        modifier = Modifier.weight(0.25f),
                        contentAlignment = Alignment.Center
                    ) {
                        if (items[idx].isSelected) {
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