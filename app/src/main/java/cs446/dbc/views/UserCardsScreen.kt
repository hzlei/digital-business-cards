package cs446.dbc.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import cs446.dbc.MainActivity
import cs446.dbc.components.BusinessCardContainer
import cs446.dbc.viewmodels.AppViewModel

@Composable
fun UserCardsScreen(cardsList: List<Any>/*, appViewModel: AppViewModel*/) {
    //appViewModel.updateScreenTitle("My Cards")
    Column (
        modifier = Modifier
            .fillMaxSize()
    ) {
        Box(
            modifier = Modifier.weight(0.9f)
        ) {
            LazyColumn(
            ) {
                items(cardsList) { card ->
                    println(card)
                    BusinessCardContainer()
                }
            }
        }
        Box(modifier = Modifier.weight(0.1f)
        ) {
            Row (
                verticalAlignment = Alignment.CenterVertically
            ){
                OutlinedButton(
                    onClick = { /*TODO: Go to business card creation screen*/ }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AddCircleOutline,
                        contentDescription = "Add Cards"
                    )
                }
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun UserCardsScreenPreview() {
    UserCardsScreen(cardsList = listOf())
}