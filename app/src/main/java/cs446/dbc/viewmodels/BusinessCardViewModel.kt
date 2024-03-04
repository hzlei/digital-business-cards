package cs446.dbc.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import cs446.dbc.models.BusinessCardModel
import cs446.dbc.models.CardType
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class BusinessCardViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val viewModelContext: CardType
): ViewModel() {

    private val myBusinessCardsContext = "myBusinessCards"
    private val sharedBusinessCardsContext = "sharedBusinessCards"

    // TODO: How will we handle injection of saved preferences??
    val myBusinessCards = savedStateHandle.getStateFlow(myBusinessCardsContext, mutableListOf<BusinessCardModel>())
    val sharedBusinessCards = savedStateHandle.getStateFlow(sharedBusinessCardsContext, mutableListOf<BusinessCardModel>())

    private val currContext = savedStateHandle.getStateFlow("cardContext", if (viewModelContext == CardType.PERSONAL) myBusinessCardsContext else sharedBusinessCardsContext)


    // TODO: Do we need a separate remove card action when removing the card?
    fun performAction(action: BusinessCardAction) {
        when (action) {
            is BusinessCardAction.ToggleFavorite -> toggleFavorite(action.cardId)
            is BusinessCardAction.InsertField -> TODO()
            is BusinessCardAction.PopulateCard -> populateCard(action)
            is BusinessCardAction.RemoveField -> TODO()
            is BusinessCardAction.UpdateAllFields -> TODO()
            is BusinessCardAction.UpdateBack -> TODO()
            is BusinessCardAction.UpdateField -> TODO()
            is BusinessCardAction.UpdateFront -> TODO()
            is BusinessCardAction.UpdateCardType -> TODO()
            is BusinessCardAction.UpdateCardContext -> updateCardContext(action.newContext)
        }
    }

    private fun updateCardContext(newContext: CardType) {
        savedStateHandle["cardContext"] = if (newContext == CardType.PERSONAL) myBusinessCardsContext else sharedBusinessCardsContext
    }

    private fun toggleFavorite(cardId: UUID) {
        val cardList = savedStateHandle.get<MutableList<BusinessCardModel>>(currContext.value) ?: return
        val updatedList = cardList.map { if (it.id == cardId) it.copy(favorite = !it.favorite) else it }
        savedStateHandle[currContext.value] = updatedList
    }




    fun getContext(): String {
        return currContext.value
    }
    private fun populateCard(action: BusinessCardAction.PopulateCard) {
        val newCard = BusinessCardModel(
            id = UUID.randomUUID(),
            front = action.front,
            back = action.back,
            favorite = action.favorite,
            fields = action.fields,
            cardType = action.cardType
        )
        val currCards = savedStateHandle.get<MutableList<BusinessCardModel>>(currContext.value)
        currCards?.add(newCard)
        savedStateHandle[currContext.value] = currCards
    }
}