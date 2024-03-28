package cs446.dbc.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Flip
import androidx.compose.material.icons.outlined.Login
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cs446.dbc.models.EventModel
import cs446.dbc.models.TemplateType
import cs446.dbc.viewmodels.BusinessCardAction
import cs446.dbc.viewmodels.EventAction
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EventCard(eventModel: EventModel, onAction: (EventAction) -> Unit, onClickAction: () -> Unit) {

    // This will only toggle the dialog
    var showDialogState by rememberSaveable {
        mutableStateOf(false)
    }
    var selected by rememberSaveable {
        mutableStateOf(false)
    }

    val backgroundColor = animateColorAsState(
        targetValue = if (selected) CardDefaults.cardColors().containerColor else Color.Transparent,
        label = "EventCardContainerBackground",
    )
    val animatedPadding by animateDpAsState(
        if (selected) 16.dp else 0.dp,
        label = "padding"
    )


    val toggleSelected = { selected = !selected }

    Card (
        modifier = Modifier
            .animateContentSize(
                animationSpec = tween(
                    durationMillis = 300,
                    easing = LinearOutSlowInEasing,
                )
            )
            .graphicsLayer { clip = false },
        colors = CardDefaults.cardColors(containerColor = backgroundColor.value),
        shape = if (selected) CardDefaults.shape else RectangleShape,
        onClick = toggleSelected
    ) {
        Card(modifier = Modifier
            .padding(animatedPadding)
            .graphicsLayer { clip = false },
            shape = RoundedCornerShape(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .aspectRatio(5f / 3f)
                    .background(MaterialTheme.colorScheme.surfaceTint)
                    .wrapContentSize(Alignment.Center)
            ) {
                Column {
                    Text(eventModel.name, fontSize = 30.sp)
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(eventModel.location, fontSize = 30.sp)
                    Spacer(modifier = Modifier.height(3.dp))
                    Text("Starts ${Date(eventModel.startDate.toLong()).toFormattedString()}", fontSize = 30.sp)
                    Text("Ends ${Date(eventModel.endDate.toLong()).toFormattedString()}", fontSize = 30.sp)
                }
            }
        }
        AnimatedVisibility(
            selected,
            modifier = Modifier.fillMaxSize()
        ) {
            // Business Card Toolbar
            Row(
                modifier = Modifier
                    .weight(0.5f)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = { onAction(EventAction.RemoveEvent(eventModel)) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Outlined.Delete, "Delete", tint = Color.Red)
                }
                TextButton(
                    onClick = onClickAction,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.AutoMirrored.Outlined.Login, "Enter")
                }
            }
        }
    }
}

fun Date.toFormattedString(): String {
    val simpleDateFormat = SimpleDateFormat("LLLL dd, yyyy", Locale.getDefault())
    return simpleDateFormat.format(this)
}