package cs446.dbc.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import cs446.dbc.models.BusinessCardModel
import cs446.dbc.models.Field
import cs446.dbc.models.FieldType
import cs446.dbc.viewmodels.BusinessCardViewModel


@Preview
@Composable
fun BusinessCard(cardModel: BusinessCardModel?) {
    // This will only toggle the dialog
    // TODO: We need to reference the specific card's data to share
    // TODO: maybe add a StateFlow for data, holds currently sharing card data
    var showDialogState by rememberSaveable {
        mutableStateOf(false)
    }
    var selected by rememberSaveable {
        mutableStateOf(false)
    }
    var cardFace by rememberSaveable {
        mutableStateOf(CardFace.Front)
    }
    val toggleSelected = { selected = !selected }
    val animatedPadding by animateDpAsState(
        if (selected) {
            16.dp
        } else {
            0.dp
        },
        label = "padding"
    )

    val cardViewModel: BusinessCardViewModel = viewModel() { BusinessCardViewModel(
        front = cardModel?.front ?: "",
        back = cardModel?.back ?: "",
        favorite = cardModel?.favorite ?: false,
        fields = cardModel?.fields ?: mutableListOf<Field>(),
        savedStateHandle = createSavedStateHandle()
    )
    }

//        factory = BusinessCardViewModel(
//
//    ),
//    )
    val cardFront by cardViewModel.front.collectAsStateWithLifecycle()
    val cardBack by cardViewModel.back.collectAsStateWithLifecycle()
    val cardFavorite by cardViewModel.favorite.collectAsStateWithLifecycle()
    val cardFields by cardViewModel.fields.collectAsStateWithLifecycle()

    Card(
        modifier = Modifier.animateContentSize(
            animationSpec = tween(
                durationMillis = 300,
                easing = LinearOutSlowInEasing,
            )
        ),
        shape = if (selected) CardDefaults.shape else RectangleShape,
        onClick = toggleSelected
    ) {
        Box(modifier = Modifier.padding(animatedPadding)) {
            FlipCard(cardFace = cardFace, onClick = { toggleSelected() },
                front = {
                    Face(Color.Red, cardFront)
                },
                back = {
                    Face(Color.Blue, cardBack)
                }
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
                                     cardViewModel.updateCardFavourite(!cardFavorite)
                }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Outlined.Star, "Favorite")
                }
                // TODO: need to show fields
                TextButton(onClick = {}, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Outlined.Edit, "Edit")
                }
            }
        }
    }
    if (showDialogState) {
        ShareDialog {
            println("Favourite = ${cardFavorite}")
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