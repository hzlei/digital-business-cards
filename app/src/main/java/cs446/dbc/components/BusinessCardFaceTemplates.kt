package cs446.dbc.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
fun Template1(background: Color, cardData: BusinessCardModel, isFront: Boolean) {
    Box(
        modifier = Modifier
            .aspectRatio(5f / 3f) // maintain aspect ratio
            .background(background)
            .wrapContentSize(Alignment.Center)
    ) {
//        Text(text, fontSize = 30.sp)
        val cardFieldMap = cardData.fields.associateBy({it.name}, {it.value})
        val cardName = cardFieldMap.get("Full Name").toString()
        val cardEmail = cardFieldMap.get("Email").toString()
        val cardOrg = cardFieldMap.get("Organization").toString()
        if (isFront) {
            // front card layout
            Column {
                Text(cardName, fontSize = 30.sp)
                Text(cardEmail, fontSize = 20.sp)
                Text(cardOrg, fontSize = 20.sp)
            }
        }
        else {
            // back card layout
            Text(cardName, fontSize = 30.sp)
        }
    }
}

