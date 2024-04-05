package cs446.dbc.components

import android.content.Context
import android.graphics.Bitmap
import android.os.Parcelable
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import cs446.dbc.api.ApiFunctions
import cs446.dbc.models.BusinessCardModel
import cs446.dbc.models.CardType
import cs446.dbc.viewmodels.BusinessCardAction
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.FileFilter

private enum class ShareDialogViews {
    Options, Bluetooth, QRCode, NearbyShare;
}

@Parcelize
@Serializable
data class QRCodeCardInfo (
    val userId: String,
    val cardId: String
): Parcelable

@Composable
fun ShareDialog(
    cardModel: BusinessCardModel,
    userId: String,
    onAction: (BusinessCardAction) -> Unit,
    onDismissRequest: () -> Unit = {}
) {
    var currentView by remember { mutableStateOf(ShareDialogViews.Options) }
    var qrCodeBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    val context = LocalContext.current

    AlertDialog(
        icon = {
            Icon(Icons.Outlined.Share, "Share Card")
        },
        title = {
            Text("Share Card")
        },
        onDismissRequest = onDismissRequest,
        text = {

            AnimatedContent(
                targetState = currentView,
                label = "ShareDialogView",
            ) { view ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    when (view) {
                        ShareDialogViews.Options -> {
                            ShareButton(text = "Bluetooth", icon = Icons.Rounded.Bluetooth) {
                                onAction(BusinessCardAction.ShareCardBluetooth(cardModel))
                            }
                            ShareButton(text = "QR Code", icon = Icons.Rounded.QrCode2) {
                                val jsonCode = Json.encodeToString(QRCodeCardInfo(userId, cardModel.id))
                                Log.d("STUPID JSON", jsonCode)
                                qrCodeBitmap = generateQRCode(jsonCode)

                                // Add card to user in server
                                ApiFunctions.addUserCard(cardModel, userId)
                                // upload images
                                val directory = context.getExternalFilesDir(null)!!
                                val frontImage = directory.listFiles(FileFilter { file ->
                                    file.name == cardModel.front
                                })
                                val backImage = directory.listFiles(FileFilter { file ->
                                    file.name == cardModel.back
                                })

                                if (frontImage != null) {
                                    if (frontImage.isNotEmpty()) {
                                        ApiFunctions.uploadImage(
                                            cardModel.front,
                                            "front",
                                            userId,
                                            cardModel.id,
                                            context
                                        )
                                    }
                                }

                                if (backImage != null) {
                                    if (backImage.isNotEmpty()) {
                                        ApiFunctions.uploadImage(
                                            cardModel.back,
                                            "back",
                                            userId,
                                            cardModel.id,
                                            context
                                        )
                                    }
                                }

                                currentView = ShareDialogViews.QRCode
                            }
                        }

                        ShareDialogViews.Bluetooth -> {

                        }

                        ShareDialogViews.QRCode -> {
                            Image(
                                bitmap = qrCodeBitmap!!.asImageBitmap(),
                                contentDescription = "QR Code",
                                modifier = Modifier.size(250.dp),
                                contentScale = ContentScale.Fit
                            )
                        }

                        ShareDialogViews.NearbyShare -> TODO()
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = {
                if (currentView == ShareDialogViews.Options) {
                    onDismissRequest()
                } else {
                    currentView = ShareDialogViews.Options
                }
            }) {
                AnimatedContent(targetState = currentView,
                    label = "ShareDialogDismiss",
                    transitionSpec = { fadeIn() togetherWith fadeOut() }) { view ->
                    when (view) {
                        ShareDialogViews.Options -> {
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
private fun ShareButton(
    text: String, icon: ImageVector, iconDescription: String = "", onBtnClick: () -> Unit
) {
    FilledTonalButton(
        onClick = { onBtnClick() },
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon, iconDescription, modifier = Modifier.size(20.dp)

            )
            Spacer(modifier = Modifier.size(4.dp))
            Text(text = text)
        }
    }
}

private fun generateQRCode(content: String): Bitmap? {

    val qrCodeWriter = QRCodeWriter()

    try {
        val bits = qrCodeWriter.encode(
            content, BarcodeFormat.QR_CODE, 512, 512, mapOf(
                EncodeHintType.CHARACTER_SET to "UTF-8"
            )
        )
        val bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.RGB_565)
        for (x in 0 until 512) {
            for (y in 0 until 512) {
                bitmap.setPixel(
                    x,
                    y,
                    if (bits[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                )
            }
        }
        return bitmap
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}