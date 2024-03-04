package cs446.dbc.components

import android.content.res.Resources
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import cs446.dbc.models.BusinessCardModel

// Name, email, organization <Required>
// phone number, Linkedin, GitHub <Optional>
@Composable
fun Face(background: Color, business_card_data: BusinessCardModel) {
    Box(
        modifier = Modifier
            .aspectRatio(5f / 3f) // maintain aspect ratio
            .background(background)
            .wrapContentSize(Alignment.Center)
    ) {
        Text(text, fontSize = 30.sp)
        business_card_data.fields
    }
}
