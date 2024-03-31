package cs446.dbc.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cs446.dbc.models.BusinessCardModel
import cs446.dbc.models.EventModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CreateEditViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
): ViewModel() {
    // Existing declarations for StateFlows
    private val _createEditBusinessCard = MutableStateFlow(BusinessCardModel())
    val createEditBusinessCard = _createEditBusinessCard.asStateFlow()

    private val _createEditEvent = MutableStateFlow(EventModel())
    val createEditEvent = _createEditEvent.asStateFlow()

    private val _eventBusinessCardList = MutableStateFlow(mutableListOf<BusinessCardModel>())
    val eventBusinessCardList = _eventBusinessCardList.asStateFlow()

    // Functions to update StateFlows

    fun setEditingBusinessCard(card: BusinessCardModel) {
        viewModelScope.launch {
            _createEditBusinessCard.value = card
        }
    }

    fun setEditingEvent(event: EventModel) {
        viewModelScope.launch {
            _createEditEvent.value = event
        }
    }

    fun addCardToEvent(card: BusinessCardModel) {
        viewModelScope.launch {
            val currentList = _eventBusinessCardList.value.toMutableList()
            if (!currentList.any { it.id == card.id }) {
                currentList.add(card)
                _eventBusinessCardList.value = currentList
            }
        }
    }

    fun removeCardFromEvent(cardId: String) {
        viewModelScope.launch {
            val currentList = _eventBusinessCardList.value.toMutableList()
            currentList.removeAll { it.id == cardId }
            _eventBusinessCardList.value = currentList
        }
    }

    // Additional functions as necessary for handling business card or event updates
}
