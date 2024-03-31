package cs446.dbc.views

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import cs446.dbc.models.Field
import cs446.dbc.models.FieldType
import cs446.dbc.viewmodels.BusinessCardViewModel
import java.util.*

@Composable
fun CreateBusinessCardScreen(
    cardViewModel: BusinessCardViewModel,
    navController: NavHostController
) {
    // State holders for each card detail to be inputted by the user.
    var frontText by remember { mutableStateOf("") }
    var backText by remember { mutableStateOf("") }
    var favorite by remember { mutableStateOf(false) }
    // Add more details as necessary.

    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = frontText,
            onValueChange = { frontText = it },
            label = { Text("Front Text") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = backText,
            onValueChange = { backText = it },
            label = { Text("Back Text") },
            modifier = Modifier.fillMaxWidth()
        )

        // Implement toggles or other inputs for additional card details.

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                // Assume creation of a simple business card without additional fields.
                // Implement field addition as needed.
                cardViewModel.createNewCard(
                    front = frontText,
                    back = backText,
                    favorite = favorite,
                    fields = mutableListOf(Field(name = "Sample Field", value = "Sample Value", type = FieldType.TEXT)), // Example field. Adjust based on actual data model.
                    cardType = cs446.dbc.models.CardType.PERSONAL // Adjust based on user selection or context.
                )
                navController.popBackStack() // Navigate back to the previous screen.
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Create Card")
        }
    }
}
