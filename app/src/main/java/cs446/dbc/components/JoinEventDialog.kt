package cs446.dbc.components


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.GroupAdd
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import cs446.dbc.viewmodels.BusinessCardViewModel
import cs446.dbc.viewmodels.CreateEditViewModel

@Composable
fun JoinEventDialog(cardViewModel: BusinessCardViewModel, createEditViewModel: CreateEditViewModel, onDismiss: () -> Unit, onClick: (eventId: String) -> Unit) {

    var eventId by rememberSaveable {
        mutableStateOf("")
    }

    AlertDialog(
        icon = {
            Icon(Icons.Outlined.Event, "Join Event")
        },
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onClick(eventId) }) {
                Text(text = "Join")
            }
        },
        title = { Text(text = "Join Event") },
        text = {
            Column (
                modifier = Modifier.height(300.dp)
            ){
                OutlinedTextField(
                    value = eventId, 
                    onValueChange = {
                        eventId = it
                    },
                    placeholder = {
                        Text(text = "A23dsfrwe255")
                    },
                    label = {
                        Text(text = "Event ID")
                    },
                    modifier = Modifier.weight(0.2f)
                )
                
                Spacer(modifier = Modifier.height(3.dp))
                
                Box (
                    modifier = Modifier.weight(0.8f)
                ) {
                    BusinessCardMultiSelect(
                        title = "Select Any Business Cards to Upload to Event",
                        cardViewModel = cardViewModel,
                        createEditViewModel = createEditViewModel
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(text = "Dismiss")
            }
        }
    )
}

@Composable
private fun AddEventButton(
    text: String,
    icon: ImageVector,
    iconDescription: String = "",
    onBtnClick: () -> Unit
) {
    FilledTonalButton(
        onClick = { onBtnClick() },
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                iconDescription,
                modifier = Modifier
                    .size(20.dp)

            )
            Spacer(modifier = Modifier.size(4.dp))
            Text(text = text)
        }
    }
}