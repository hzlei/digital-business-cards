package cs446.dbc.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import cs446.dbc.models.BusinessCardModel

// Name, email, organization <Required>
// phone number, Linkedin, GitHub <Optional>
@Composable
fun Template1(background: Color, card_data: BusinessCardModel, isFront: Boolean) {
    Box(
        modifier = Modifier
            .aspectRatio(5f / 3f) // maintain aspect ratio
            .background(background)
            .wrapContentSize(Alignment.Center)
    ) {
//        Text(text, fontSize = 30.sp)
        val card_field_map = card_data.fields.associateBy{it.name}
        card_field_map.get("Full Name")
        card_field_map.get("Email")
        card_field_map.get("Organization")
        if (isFront) {
            // front card layout

        }
        else {
            // back card layout

        }
    }
}

