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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import cs446.dbc.viewmodels.BusinessCardViewModel
import cs446.dbc.viewmodels.CreateEditViewModel

@Composable
fun JoinEventDialog(cardViewModel: BusinessCardViewModel, createEditViewModel: CreateEditViewModel, onDismiss: () -> Unit, onClick: () -> Unit) {
    AlertDialog(
        icon = {
            Icon(Icons.Outlined.Event, "Join Event")
        },
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onClick() }) {
                Text(text = "Join")
            }
        },
        title = { Text(text = "Join Event") },
        text = {
            Box (
                modifier = Modifier.height(200.dp)
            ){
                BusinessCardMultiSelect(
                    title = "Select Business Cards to Upload to Event",
                    cardViewModel = cardViewModel,
                    createEditViewModel = createEditViewModel
                )
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