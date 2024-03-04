package cs446.dbc.components

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import org.json.JSONObject

@Preview(
    wallpaper = Wallpapers.BLUE_DOMINATED_EXAMPLE
)
@Composable
fun ShareDialog(onDismissRequest: () -> Unit = {}) {
    var showQRCode by remember { mutableStateOf(false) }
    var qrCodeBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

    if (showQRCode && qrCodeBitmap != null) {
        Dialog(onDismissRequest = { showQRCode = false }) {
            Image(
                bitmap = qrCodeBitmap!!.asImageBitmap(),
                contentDescription = "QR Code",
                modifier = Modifier.size(250.dp),
                contentScale = ContentScale.Fit
            )
        }
    }

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
                ShareButton(text = "QR Code", icon = Icons.Rounded.QrCode2) {
                    val jsonData = JSONObject().apply {
                        put("test", "value")
                    }
                    qrCodeBitmap = QRCode(jsonData)
                    showQRCode = true
                }
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

// Move this elsewhere
@Composable
private fun TestQRCodeReader() {
    var qrCodeResult by rememberSaveable { mutableStateOf("") }

    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            qrCodeResult = result.contents
            // we now have the QR code data
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

private fun QRCode(data: JSONObject): Bitmap? {
    val content = data.toString()

    val qrCodeWriter = QRCodeWriter()

    try {
        val bits = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, 512, 512, mapOf(
            EncodeHintType.CHARACTER_SET to "UTF-8"
        ))
        val bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.RGB_565)
        for (x in 0 until 512) {
            for (y in 0 until 512) {
                bitmap.setPixel(x, y, if(bits[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        return bitmap
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}

//@Preview(showSystemUi = true)
//@Composable
//fun ShareDialogPreview() {
//    ShareDialog() {
//    }
//}
