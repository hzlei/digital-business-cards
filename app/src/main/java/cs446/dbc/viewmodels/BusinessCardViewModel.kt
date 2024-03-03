package cs446.dbc.viewmodels

import androidx.lifecycle.ViewModel
import cs446.dbc.models.BusinessCardModel
import cs446.dbc.models.Field
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class BusinessCardViewModel: ViewModel() {
    private val _uiState = MutableStateFlow(BusinessCardModel("", "", false, mutableListOf<Field>()))
    val uiState: StateFlow<BusinessCardModel> = _uiState.asStateFlow()


    fun updateCard(front: String, back: String, favourite: Boolean, fields: MutableList<Field>) {
        updateCardFront(front)
        updateCardBack(back)
        updateCardFavourite(favourite)
        updateCardFields(fields)
    }

    fun updateCardFront(front: String) {
        _uiState.update { currentState ->
            currentState.copy(front = front)
        }
    }

    fun updateCardBack(back: String) {
        _uiState.update { currentState ->
            currentState.copy(back = back)
        }
    }

    fun updateCardFavourite(favorite: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(favorite = favorite)
        }
    }

    fun updateCardFields(fields: MutableList<Field>) {
        _uiState.update { currentState ->
            currentState.copy(fields = fields)
        }
    }

    fun insertCardField(field:Field) {
        val fields: MutableList<Field> = _uiState.value.fields
        fields.add(field)
        updateCardFields(fields)
    }

    fun removeCardField(field:Field) {
        val fields: MutableList<Field> = _uiState.value.fields
        fields.remove(field)
        updateCardFields(fields)
    }

    fun updateCardField(oldField: Field, newField: Field) {
        removeCardField(oldField)
        insertCardField(newField)
    }




}