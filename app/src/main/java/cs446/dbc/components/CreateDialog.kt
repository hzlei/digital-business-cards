package cs446.dbc.components

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

@Composable
fun CreateDialog(snackbarHostState: SnackbarHostState, userId: String, onDismissRequest: () -> Unit = {}) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(true) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    // For now, the image is always set to be the front.
    // Should be changed once we have an actual business card creation screen
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch(Dispatchers.IO) {
                saveImageToStorage(context, it, userId, isFront = true) { savedFile ->
                    // Update the state to display the image in the dialog
                    imageUri = Uri.fromFile(savedFile)
                    showDialog = true
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Select Image") },
            text = {
                Column(modifier = Modifier.padding(8.dp)) {
                    Button(onClick = { galleryLauncher.launch("image/*") }) {
                        Text("Open Gallery")
                    }
                    // Below is how you display an image
                    imageUri?.let { uri ->
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = "Selected Image",
                            modifier = Modifier
                                .padding(top = 8.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text("Dismiss")
                }
            }
        )
    }
}

fun saveImageToStorage(context: Context, imageUri: Uri, userId: String, isFront: Boolean, onSaved: (File) -> Unit) {
    // rename the cardID after the image upload
    // save it right now as user_$userId_card__image_$cardSide
    // then later on during the save process for the new card, we'll find this image and rename it
    // with the correct card ID

    // This function saves images to local storage for later use
    val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
    val directory = context.getExternalFilesDir(null) ?: return
    // Use the following file name convention: user_$userId_card_$cardId_image_$cardSide
    val side = if (isFront) "front" else "back"
    val file = File(directory, "user_${userId}__image_${side}")
    inputStream?.use { input ->
        FileOutputStream(file).use { output ->
            input.copyTo(output)
        }
        onSaved(file)
    }
}
