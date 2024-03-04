package cs446.dbc.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import cs446.dbc.components.BusinessCard
import cs446.dbc.models.BusinessCardModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class BusinessCardViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle
): ViewModel() {

    // TODO: How will we handle injection of saved preferences??
    val businessCard = savedStateHandle.getStateFlow("businessCardList", mutableListOf<BusinessCardModel>())

    // TODO: could move these into a dictionary so we don't need to call each one individually
//    val id = savedStateHandle.getStateFlow("id", UUID.randomUUID())
//    val front = savedStateHandle.getStateFlow("front", front)
//    val back = savedStateHandle.getStateFlow("back", back)
//    val favorite = savedStateHandle.getStateFlow("favorite", favorite)
//    val fields = savedStateHandle.getStateFlow("fields", fields)


//    private val _uiState = MutableStateFlow(BusinessCardModel(id, front, back, favorite, fields))
//    val uiState: StateFlow<BusinessCardModel> = _uiState.asStateFlow()

//    fun updateCard(front: String, back: String, favourite: Boolean, fields: MutableList<Field>) {
//        updateCardFront(front)
//        updateCardBack(back)
//        updateCardFavourite(favourite)
//        updateCardFields(fields)
//    }
//
//    fun updateCardFront(front: String) {
//        savedStateHandle["front"] = front
//    }
//
//    fun updateCardBack(back: String) {
//        savedStateHandle["back"] = back
//    }
//
//    fun updateCardFavourite(favorite: Boolean) {
//        savedStateHandle["favorite"] = favorite
//    }
//
//    fun updateCardFields(fields: MutableList<Field>) {
//        savedStateHandle["fields"] = fields
//    }
//
//    fun insertCardField(field:Field) {
//        val fields: MutableList<Field> = savedStateHandle.get<MutableList<Field>>("fields") ?: mutableListOf()
//        fields.add(field)
//        updateCardFields(fields)
//    }
//
//    fun removeCardField(field:Field) {
//        val fields: MutableList<Field> = savedStateHandle.get<MutableList<Field>>("fields") ?: mutableListOf()
//        fields.remove(field)
//        updateCardFields(fields)
//    }
//
//    fun updateCardField(oldField: Field, newField: Field) {
//        removeCardField(oldField)
//        insertCardField(newField)
//    }

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
        val cardList = savedStateHandle.get<MutableList<BusinessCardModel>>("businessCards")!!
        var currCard = cardList.find { it.id == cardId }
        if (currCard != null) {
            currCard.favorite = !currCard.favorite
        }
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