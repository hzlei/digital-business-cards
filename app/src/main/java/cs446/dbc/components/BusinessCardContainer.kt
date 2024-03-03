package cs446.dbc.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Flip
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview
@Composable
fun BusinessCardContainer() {
    var selected by rememberSaveable {
        mutableStateOf(false)
    }
    Card(
        modifier = Modifier.animateContentSize(
            animationSpec = tween(
                durationMillis = 300,
                easing = LinearOutSlowInEasing,
            )
        ),
        shape = if (selected) CardDefaults.shape else RectangleShape,
        onClick = {selected = !selected}
    ) {
        Box(modifier = Modifier.padding(if (selected) 16.dp else 0.dp)) {
            BusinessCard()
        }
        AnimatedVisibility(
            selected,
            enter = slideInVertically(),
            exit = slideOutVertically(),
            ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(onClick = {}, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Outlined.Flip, "Flip")
                }
                FilledTonalButton(onClick = {}, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Outlined.Share, "Share")
                }
                FilledTonalButton(onClick = {}, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Outlined.Star, "Favorite")
                }
                FilledTonalButton(onClick = {}, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Outlined.Edit, "Edit")
                }
            }
        }
    }
}