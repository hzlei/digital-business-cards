package cs446.dbc.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Flip
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
import cs446.dbc.models.BusinessCardModel
import cs446.dbc.models.CardType
import cs446.dbc.models.TemplateType
import cs446.dbc.viewmodels.BusinessCardAction


@Composable
fun BusinessCard(cardModel: BusinessCardModel, onAction: (BusinessCardAction) -> Unit) {
    // This will only toggle the dialog
    var showDialogState by rememberSaveable {
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

    val toggleSelected = { selected = !selected }

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
                        Template1(
                            background = MaterialTheme.colorScheme.surfaceTint,
                            card_data = cardModel,
                            isFront = true
                        )
                    }
                    back = {
                        Template1(
                            background = MaterialTheme.colorScheme.surfaceBright,
                            card_data = cardModel,
                            isFront = false
                        )
                    }
                }

                else -> {
                    front = { Face(MaterialTheme.colorScheme.surfaceTint, cardModel.front) }
                    back = { Face(MaterialTheme.colorScheme.surfaceBright, cardModel.back) }
                }

            }
            FlipCard(
                cardFace = cardFace,
                onClick = { toggleSelected() },
                front = front,
                back = back
            )

        }
        AnimatedVisibility(
            selected,
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(onClick = {
                    cardFace = cardFace.next
                }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Outlined.Flip, "Flip")
                }
                TextButton(onClick = { showDialogState = true }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Outlined.Share, "Share")
                }
                TextButton(onClick = {
                    onAction(BusinessCardAction.ToggleFavorite(cardModel.id))
                }, modifier = Modifier.weight(1f)) {
                    Icon(
                        if (cardModel.favorite) Icons.Outlined.Star else Icons.Outlined.StarOutline,
                        "Favorite"
                    )
                }
                // TODO: need to show fields
                AnimatedVisibility(visible = cardModel.cardType == CardType.PERSONAL) {
                    TextButton(onClick = {}, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Outlined.Edit, "Edit")
                    }
                }
            }
        }
    }
    if (showDialogState) {
        ShareDialog(cardModel) {
            showDialogState = false
        }
    }
}


@Composable
fun Face(background: Color, text: String) {
    Box(
        modifier = Modifier
            .aspectRatio(5f / 3f)
            .background(background)
            .wrapContentSize(Alignment.Center)
    ) {
        Text(text, fontSize = 30.sp)
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