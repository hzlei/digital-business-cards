package cs446.dbc.components

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.with
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import cs446.dbc.models.BusinessCardModel
import cs446.dbc.viewmodels.BusinessCardAction
import cs446.dbc.viewmodels.BusinessCardViewModel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private enum class ReceiveDialogViews {
    Options,
    Bluetooth,
    QRCode,
    NearbyShare;
}

@Composable
fun ReceiveDialog(sharedCardViewModel: BusinessCardViewModel, onDismissRequest: () -> Unit = {}) {
    var currentView by remember { mutableStateOf(ReceiveDialogViews.Options) }

    AlertDialog(
        icon = {
            Icon(Icons.Outlined.Download, "Receive Card")
        },
        title = {
            Text("Receive Card")
        },
        onDismissRequest = onDismissRequest,
        text = {

            AnimatedContent(
                targetState = currentView,
                label = "ReceiveDialogView",
            ) { view ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    when (view) {
                        ReceiveDialogViews.Options -> {
                            ReceiveButton(text = "Bluetooth", icon = Icons.Rounded.Bluetooth) {}
                            ReceiveButton(text = "QR Code", icon = Icons.Rounded.QrCode2) {
                                currentView = ReceiveDialogViews.QRCode
                            }
                            ReceiveButton(text = "Nearby Share", icon = Icons.Rounded.Wifi) {}
                        }

                        ReceiveDialogViews.Bluetooth -> {

                        }

                        ReceiveDialogViews.QRCode -> {
                            ReadQRCode(sharedCardViewModel)
                        }

                        ReceiveDialogViews.NearbyShare -> {

                        }
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = {
                if (currentView == ReceiveDialogViews.Options) {
                    onDismissRequest()
                } else {
                    currentView = ReceiveDialogViews.Options
                }
            }) {
                AnimatedContent(
                    targetState = currentView,
                    label = "ShareDialogDismiss",
                    transitionSpec = { fadeIn() togetherWith fadeOut() }
                ) { view ->
                    when (view) {
                        ReceiveDialogViews.Options -> {
                            Text(text = "Dismiss")
                        }

                        else -> {
                            Text(text = "Back")
                        }
                    }

                }
            }
        },
        confirmButton = {},
    )
}

@Composable
private fun ReceiveButton(
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

@Composable
private fun ReadQRCode(sharedCardViewModel: BusinessCardViewModel) {
    var qrCodeResult by rememberSaveable { mutableStateOf("") }

    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            qrCodeResult = result.contents
            val card = Json.decodeFromString<BusinessCardModel>(qrCodeResult)

            sharedCardViewModel.performAction(BusinessCardAction.InsertCard(card))
        }
    }

    Column {
        FilledTonalButton(onClick = {
            // Configure scan options
            val options = ScanOptions()
            options.setPrompt("Scan a QR code")
            options.setBeepEnabled(true)
            options.setOrientationLocked(false)
            options.setBarcodeImageEnabled(true)
            scanLauncher.launch(options)
        }) {
            Text("Scan QR Code")
        }

        if (qrCodeResult.isNotEmpty()) {
            Text("Scanned QR Code: $qrCodeResult")
        }
    }
}
