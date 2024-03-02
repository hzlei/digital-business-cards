package cs446.dbc.components

import android.content.Context
import android.view.Gravity
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider

@Composable
fun ShareDialog(onDismissRequest: () -> Unit) {

    Dialog(onDismissRequest = {onDismissRequest()}, properties = DialogProperties(dismissOnBackPress = true)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .padding(5.dp) ,
            shape = RoundedCornerShape(15.dp)
        ) {
            val dialogWindowProvider = LocalView.current.parent as DialogWindowProvider
            dialogWindowProvider.window.setGravity(Gravity.CENTER)
            Column (
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center),
                horizontalAlignment = Alignment.End
            ) {
                ShareButton(text = "Bluetooth", icon = Icons.Rounded.Bluetooth) {
                    Toast.makeText(this as Context, "Bluetooth", Toast.LENGTH_SHORT).show()
                }
                ShareButton(text = "QR Code", icon = Icons.Rounded.QrCode2) {}
                ShareButton(text = "Nearby Share", icon = Icons.Rounded.Wifi) {}
                Spacer(modifier = Modifier.height(2.dp))
                TextButton(onClick = { onDismissRequest() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.hsl(206.0F, 0.8F, 0.55F))) {
                    Text(text = "Dismiss")
                }
            }
        }
    }
}

@Composable
private fun ShareButton(text: String, icon: ImageVector, iconDescription: String = "", onBtnClick: () -> Unit) {
    FilledTonalButton(
        onClick = { onBtnClick() },
        modifier = Modifier
            .size(200.dp, 50.dp)
            .clip(RoundedCornerShape(10.dp))
            .padding(4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.hsl(206.0F, 0.8F, 0.65F)
        )
    ) {
        Icon(imageVector = icon,
            iconDescription,
            modifier = Modifier
                .size(20.dp)
        )
        Spacer(modifier = Modifier.size(5.dp))
        Text(text = text)
    }
}

//@Preview(showSystemUi = true)
//@Composable
//fun ShareDialogPreview() {
//    ShareDialog() {
//    }
//}