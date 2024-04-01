package cs446.dbc.components

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Flip
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import coil.compose.rememberAsyncImagePainter
import cs446.dbc.models.BusinessCardModel
import cs446.dbc.models.CardType
import cs446.dbc.models.TemplateType
import cs446.dbc.viewmodels.BusinessCardAction
import java.io.File


@Composable
fun BusinessCard(cardModel: BusinessCardModel, isEnabled: Boolean = true, navController: NavController, onAction: (BusinessCardAction) -> Unit) {
    // This will only toggle the dialog
    var showShareDialogState by rememberSaveable {
        mutableStateOf(false)
    }

    var showDeleteDialogState by rememberSaveable {
        mutableStateOf(false)
    }

    var selected by rememberSaveable {
        mutableStateOf(false)
    }
    var cardFace by rememberSaveable {
        mutableStateOf(CardFace.Front)
    }
    val backgroundColor = animateColorAsState(
        targetValue = if (selected) CardDefaults.cardColors().containerColor else Color.Transparent,
        label = "BusinessCardContainerBackground",
    )
    val animatedPadding by animateDpAsState(
        if (selected) 16.dp else 0.dp,
        label = "padding"
    )

    val toggleSelected = { selected = !selected && isEnabled}

    Card(
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
        Box(modifier = Modifier
            .padding(animatedPadding)
            .graphicsLayer { clip = false }
        ) {
            var front: @Composable () -> Unit = {};
            var back: @Composable () -> Unit = {};
            when (cardModel.template) {
                TemplateType.TEMPLATE_1 -> {
                    front = {
                        // TODO:
                        // the background needs to be changed to an image if cardModel.front isnt an empty string
                        // otherwise, put in the surface tint
                        Face(
                            context = LocalContext.current,
                            background = MaterialTheme.colorScheme.surfaceTint,
                            imagePath = if (cardModel.front != "") cardModel.front else null,
                            text = "Front Side"
                        )
                    }
                    back = {
                        Face(
                            context = LocalContext.current,
                            background = MaterialTheme.colorScheme.surfaceTint,
                            imagePath = if (cardModel.back != "") cardModel.back else null,
                            text = "Back Side"
                        )
                    }
                }
                // TODO: add the other templates
                else -> {
                    front = { Face(LocalContext.current, MaterialTheme.colorScheme.surfaceTint, cardModel.front) }
                    back = { Face(LocalContext.current, MaterialTheme.colorScheme.surfaceBright, cardModel.back) }
                }
            }
            FlipCard(
                cardFace = cardFace,
                onClick = { toggleSelected() },
                front = front,
                back = back
            )

        }
        AnimatedVisibility(visible = selected && cardModel.fields.isNotEmpty(),
            modifier = Modifier.padding(start = animatedPadding, end = animatedPadding)) {
            HorizontalDivider(thickness = 2.dp)
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier
                    .heightIn(0.dp, 60.dp)
                    .padding(top = 4.dp, bottom = 4.dp)
            ) {
                // Card Fields
                items(cardModel.fields) { field ->
                    Row(
                        modifier = Modifier
                            .wrapContentWidth(Alignment.CenterHorizontally)
                            .fillMaxWidth()
                            .padding(start = 10.dp, end = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = field.name)
                        VerticalDivider(thickness = 20.dp, color = Color.LightGray,
                            modifier = Modifier.width(3.dp))
                        // TODO: ensure we allow for proper wrapping
                        Text(text = field.value, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
        AnimatedVisibility(visible = selected && cardModel.fields.isNotEmpty(),
            modifier = Modifier.padding(start = animatedPadding, end = animatedPadding)) {
            HorizontalDivider(thickness = 2.dp)
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
                AnimatedVisibility(visible = cardModel.cardType != CardType.EVENT_VIEW) {
                    TextButton(
                        onClick = { showDeleteDialogState = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Outlined.Delete, "Delete", tint = Color.Red)
                    }
                }
                TextButton(onClick = {
                    cardFace = cardFace.next
                }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Outlined.Flip, "Flip")
                }
                AnimatedVisibility(
                    visible = cardModel.cardType != CardType.EVENT_VIEW,
                    modifier = Modifier.weight(1f)
                ) {
                    TextButton(
                        onClick = { showShareDialogState = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Outlined.Share, "Share")
                    }
                }
                AnimatedVisibility(
                    visible = cardModel.cardType == CardType.EVENT_VIEW,
                    modifier = Modifier.weight(1f)
                ) {
                    TextButton(
                        onClick = {
                            // TODO: Send request to server for this card
                            //   use the eventId and eventUserId within the card to locate it
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Outlined.Download, "Request")
                    }
                }
                AnimatedVisibility(
                    visible = cardModel.cardType != CardType.EVENT_VIEW,
                    modifier = Modifier.weight(1f)
                ) {
                    TextButton(onClick = {
                        onAction(BusinessCardAction.ToggleFavorite(cardModel.id))
                    }, modifier = Modifier.weight(1f)) {
                        Icon(
                            if (cardModel.favorite) Icons.Outlined.Star else Icons.Outlined.StarOutline,
                            "Favorite"
                        )
                    }
                }

                AnimatedVisibility(
                    visible = cardModel.cardType == CardType.PERSONAL,
                    modifier = Modifier.weight(1f)
                ) {
                    TextButton(onClick = {
                         // TODO: Add routing to create business card screen with editing option
                         onAction(BusinessCardAction.SetCardEditFocus(cardModel.id))
                        navController.navigate("create-card")
                    }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Outlined.Edit, "Edit")
                    }
                }
            }
        }
    }
    
    if (showShareDialogState) {
        ShareDialog(cardModel, onAction) {
            showShareDialogState = false
        }
    }

    val deleteDialogRequest = {
        onAction(BusinessCardAction.RemoveCard(cardModel))
        showDeleteDialogState = false
    }

    if (showDeleteDialogState) {
        AlertDialog(onDismissRequest = { showDeleteDialogState = false },
            title = {
                    Text(text = "Delete Card")
            },
            text = {
                   Text(text = "Are you sure you want to delete this card?")
            },
            confirmButton = {
                TextButton(onClick = { deleteDialogRequest() }) {
                    Text(text = "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialogState = false
                }) {
                    Text(text = "Cancel")
                }
            }
        )
    }
}


@Composable
fun Face(context: Context, background: Color, text: String, imagePath: String? = null) {
    val imagePainter = if (!imagePath.isNullOrEmpty()) {
        rememberAsyncImagePainter(model = File(context.getExternalFilesDir(null), imagePath))
    } else {
        null
    }

    Box (
        modifier = Modifier
            .aspectRatio(5f/3f)
            .then(
                if (imagePainter != null) Modifier.background(Color.Transparent) else Modifier.background(background)
        )
        .wrapContentSize(Alignment.Center)
    ) {
        imagePainter?.let {
            Image(
                painter = it,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )
        }
        Text(
            text,
            fontSize = 30.sp,
            modifier = Modifier.align(Alignment.Center),
            textAlign = TextAlign.Center,
        )
    }
}

enum class CardFace(val angle: Float) {
    Front(0f) {
        override val next: CardFace
            get() = Back
    },
    Back(180f) {
        override val next: CardFace
            get() = Front
    };

    abstract val next: CardFace
}

@Composable
fun FlipCard(
    cardFace: CardFace,
    onClick: (CardFace) -> Unit,
    modifier: Modifier = Modifier,
    back: @Composable () -> Unit = {},
    front: @Composable () -> Unit = {},
) {
    val rotation = animateFloatAsState(
        targetValue = cardFace.angle,
        animationSpec = tween(
            durationMillis = 400,
            easing = FastOutSlowInEasing,
        ), label = "businessCardFlip"
    )
    Card(
        onClick = {
            onClick(cardFace)
        },
        modifier = modifier
            .graphicsLayer {
                rotationY = rotation.value
                cameraDistance = 12f * density
            },
        shape = RoundedCornerShape(8.dp)
    ) {
        if (rotation.value <= 90f) {
            Box {
                front()
            }
        } else {
            Box(modifier = Modifier.graphicsLayer {
                rotationY = 180f
            }) {
                back()
            }
        }
    }
}
