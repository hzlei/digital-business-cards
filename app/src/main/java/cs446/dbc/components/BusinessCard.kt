package cs446.dbc.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp

@Preview
@Composable
fun BusinessCard() {
    val front = @Composable {
        Box(
            modifier = Modifier
                .aspectRatio(5f / 3f)
                .background(Color.Red)
                .wrapContentSize(Alignment.Center)
        ) {
            Text("A", fontSize = 30.sp)
        }
    }
    val back = @Composable {
        Box(modifier = Modifier
            .aspectRatio(5f / 3f)
            .background(Color.Blue)
            .wrapContentSize(Alignment.Center)
        ) {
            Text("B", fontSize = 30.sp)
        }
    }
    front()
}