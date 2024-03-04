package cs446.dbc.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import cs446.dbc.models.BusinessCardModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class BusinessCardViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle
): ViewModel() {

    // TODO: How will we handle injection of saved preferences??
    val businessCards = savedStateHandle.getStateFlow("businessCards", mutableListOf<BusinessCardModel>())

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
        }
    }

    private fun toggleFavorite(cardId: UUID) {
        val cardList = savedStateHandle.get<MutableList<BusinessCardModel>>("businessCards") ?: return
        val updatedList = cardList.map { if (it.id == cardId) it.copy(favorite = !it.favorite) else it }
        savedStateHandle["businessCards"] = updatedList
    }

    private fun populateCard(action: BusinessCardAction.PopulateCard) {
        val newCard = BusinessCardModel(
            id = UUID.randomUUID(),
            front = action.front,
            back = action.back,
            favorite = action.favorite,
            fields = action.fields
        )
        val currCards = savedStateHandle.get<MutableList<BusinessCardModel>>("businessCards")
        currCards?.add(newCard)
        savedStateHandle["businessCards"] = currCards
    }




}