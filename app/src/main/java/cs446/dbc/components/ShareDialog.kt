package cs446.dbc.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun ShareDialog() {
    Dialog(onDismissRequest = {}, properties = DialogProperties(dismissOnBackPress = true, )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .padding(15.dp)
                .clip(RoundedCornerShape(15.dp))
        ) {
            Column (
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)
            ) {
                ShareButton(text = "Bluetooth", icon = Icons.Rounded.Bluetooth) {}
                ShareButton(text = "QR Code", icon = Icons.Rounded.QrCode2) {}
                ShareButton(text = "Nearby Share", icon = Icons.Rounded.Wifi) {}
                Button(onClick = { /*TODO*/ }) {
                    Text(text = "Dismiss")
                }
            }
        }
    }
}

@Composable
private fun ShareButton(text: String, icon: ImageVector, iconDescription: String = "", onBtnClick: () -> Unit) {
    Button(
        onClick = { onBtnClick() },
        modifier = Modifier
            .size(200.dp, 50.dp)
            .clip(RoundedCornerShape(10.dp))
            .padding(4.dp)
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

@Preview(showSystemUi = true)
@Composable
fun ShareDialogPreview() {
    ShareDialog()
}