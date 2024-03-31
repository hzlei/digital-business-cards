package cs446.dbc.viewmodels

import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import cs446.dbc.models.BusinessCardModel
import cs446.dbc.models.CardType
import cs446.dbc.models.Field
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class BusinessCardViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val viewModelContext: CardType
) : ViewModel() {

    private val myBusinessCardsContext = "myBusinessCards"
    private val sharedBusinessCardsContext = "sharedBusinessCards"

    val myBusinessCards = savedStateHandle.getStateFlow(myBusinessCardsContext, mutableListOf<BusinessCardModel>())
    val sharedBusinessCards = savedStateHandle.getStateFlow(sharedBusinessCardsContext, mutableListOf<BusinessCardModel>())

    private val currContext = savedStateHandle.getStateFlow(
        "cardContext",
        if (viewModelContext == CardType.PERSONAL) myBusinessCardsContext else sharedBusinessCardsContext
    )

    var businessCardSnapshotList: SnapshotStateList<BusinessCardModel>? = null

    fun performAction(action: BusinessCardAction) {
        when (action) {
            is BusinessCardAction.ToggleFavorite -> toggleFavorite(action.cardId)
            is BusinessCardAction.PopulateCard -> populateCard(action)
            is BusinessCardAction.InsertCard -> insertCard(action)
            is BusinessCardAction.InsertCards -> insertCards(action)
            is BusinessCardAction.RemoveCard -> removeCard(action)
            is BusinessCardAction.UpdateCardContext -> updateCardContext(action.newContext)
            // Implement or handle other actions as necessary.
        }
    }

    private fun toggleFavorite(cardId: String) {
        val cardList = savedStateHandle.get<MutableList<BusinessCardModel>>(currContext.value) ?: return
        val updatedList = cardList.map { if (it.id == cardId) it.copy(favorite = !it.favorite) else it }.toMutableList().sortedWith(compareBy({ !it.favorite }, { it.front })).toMutableList()
        savedStateHandle[currContext.value] = updatedList
        businessCardSnapshotList?.clear()
        businessCardSnapshotList?.addAll(updatedList)
    }

    private fun populateCard(action: BusinessCardAction.PopulateCard) {
        val card = BusinessCardModel(
            id = UUID.randomUUID().toString(),
            front = action.front,
            back = action.back,
            favorite = action.favorite,
            fields = action.fields,
            cardType = action.cardType
        )
        insertCard(BusinessCardAction.InsertCard(card))
    }

    private fun insertCard(action: BusinessCardAction.InsertCard) {
        val currCards = savedStateHandle.get<MutableList<BusinessCardModel>>(currContext.value)
        currCards?.add(action.card)
        currCards?.sortWith(compareBy({ !it.favorite }, { it.front }))
        savedStateHandle[currContext.value] = currCards
        businessCardSnapshotList?.clear()
        businessCardSnapshotList?.addAll(currCards!!)
    }

    private fun insertCards(action: BusinessCardAction.InsertCards) {
        val cards = savedStateHandle.get<MutableList<BusinessCardModel>>(currContext.value)
        cards?.addAll(action.cards)
        cards?.sortWith(compareBy({ !it.favorite }, { it.front }))
        savedStateHandle[currContext.value] = cards
        businessCardSnapshotList?.clear()
        businessCardSnapshotList?.addAll(cards!!)
    }

    private fun removeCard(action: BusinessCardAction.RemoveCard) {
        val cards = savedStateHandle.get<MutableList<BusinessCardModel>>(currContext.value)
        cards?.removeIf { it.id == action.card.id }
        savedStateHandle[currContext.value] = cards
        businessCardSnapshotList?.clear()
        businessCardSnapshotList?.addAll(cards!!)
    }

    private fun updateCardContext(newContext: CardType) {
        savedStateHandle["cardContext"] = if (newContext == CardType.PERSONAL) myBusinessCardsContext else sharedBusinessCardsContext
    }

    fun createNewCard(front: String, back: String, favorite: Boolean, fields: MutableList<Field>, cardType: CardType = CardType.PERSONAL) {
        val newCard = BusinessCardModel(
            id = UUID.randomUUID().toString(), // Assuming IDs are managed client-side. Adjust if necessary.
            front = front,
            back = back,
            favorite = favorite,
            fields = fields,
            cardType = cardType
        )

        // Directly insert the new card into the appropriate list.
        performAction(BusinessCardAction.Insert
