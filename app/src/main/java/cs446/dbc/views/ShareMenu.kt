package cs446.dbc.views

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import cs446.dbc.components.ShareDialog

@Composable
fun shareMenu() {

    var showDialog by rememberSaveable {
        mutableStateOf(true)
    }

    if (showDialog) {
        ShareDialog() {
            showDialog = false
        }
    }

}