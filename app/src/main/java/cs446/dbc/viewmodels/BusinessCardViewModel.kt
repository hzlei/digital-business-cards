package cs446.dbc.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import cs446.dbc.models.BusinessCardModel
import cs446.dbc.models.Field
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.internal.InjectedFieldSignature
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class BusinessCardViewModel @Inject constructor(
    front: String = "",
    back: String = "",
    favorite: Boolean = false,
    fields: MutableList<Field> = mutableListOf<Field>(),
    private val savedStateHandle: SavedStateHandle
): ViewModel() {
    val id = savedStateHandle.getStateFlow("id", UUID.randomUUID())
    val front = savedStateHandle.getStateFlow("front", front)
    val back = savedStateHandle.getStateFlow("back", back)
    val favorite = savedStateHandle.getStateFlow("favorite", favorite)
    val fields = savedStateHandle.getStateFlow("fields", fields)


//    private val _uiState = MutableStateFlow(BusinessCardModel(id, front, back, favorite, fields))
//    val uiState: StateFlow<BusinessCardModel> = _uiState.asStateFlow()

    fun updateCard(front: String, back: String, favourite: Boolean, fields: MutableList<Field>) {
        updateCardFront(front)
        updateCardBack(back)
        updateCardFavourite(favourite)
        updateCardFields(fields)
    }

    fun updateCardFront(front: String) {
        savedStateHandle["front"] = front
    }

    fun updateCardBack(back: String) {
        savedStateHandle["back"] = back
    }

    fun updateCardFavourite(favorite: Boolean) {
        savedStateHandle["favorite"] = favorite
    }

    fun updateCardFields(fields: MutableList<Field>) {
        savedStateHandle["fields"] = fields
    }

    fun insertCardField(field:Field) {
        val fields: MutableList<Field> = savedStateHandle.get<MutableList<Field>>("fields") ?: mutableListOf()
        fields.add(field)
        updateCardFields(fields)
    }

    fun removeCardField(field:Field) {
        val fields: MutableList<Field> = savedStateHandle.get<MutableList<Field>>("fields") ?: mutableListOf()
        fields.remove(field)
        updateCardFields(fields)
    }

    fun updateCardField(oldField: Field, newField: Field) {
        removeCardField(oldField)
        insertCardField(newField)
    }




}