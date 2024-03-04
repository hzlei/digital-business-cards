package cs446.dbc.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp

@Preview(
    wallpaper = Wallpapers.BLUE_DOMINATED_EXAMPLE
)
@Composable
fun ShareDialog(onDismissRequest: () -> Unit = {}) {
    AlertDialog(
        icon = {
            Icon(Icons.Outlined.Share, "Share Card")
        },
        title = {
            Text("Share Card")
        },
        onDismissRequest = onDismissRequest,
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ShareButton(text = "Bluetooth", icon = Icons.Rounded.Bluetooth) {}
                ShareButton(text = "QR Code", icon = Icons.Rounded.QrCode2) {}
                ShareButton(text = "Nearby Share", icon = Icons.Rounded.Wifi) {}
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismissRequest() }) {
                Text(text = "Dismiss")
            }
        },
        confirmButton = {},
    )
}

@Composable
private fun ShareButton(
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