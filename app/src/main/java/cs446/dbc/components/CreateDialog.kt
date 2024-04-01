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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlinx.coroutines.Dispatchers
import androidx.compose.ui.Modifier
import coil.compose.rememberImagePainter

@Composable
fun CreateDialog(snackbarHostState: SnackbarHostState, onDismissRequest: () -> Unit = {}) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(true) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch(Dispatchers.IO) {
                saveImageToStorage(context, it) { savedFile ->
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
                            painter = rememberImagePainter(uri),
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

fun saveImageToStorage(context: Context, imageUri: Uri, onSaved: (File) -> Unit) {
    // This function saves images to local storage for later use
    val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
    val directory = context.getExternalFilesDir(null) ?: return
    // Use the following file name convention: user_$userId_card_$cardId_image_$cardSide
    val file = File(directory, "selected_image_${System.currentTimeMillis()}.jpg")

    inputStream?.use { input ->
        FileOutputStream(file).use { output ->
            input.copyTo(output)
        }
        onSaved(file)
    }
}
