package cs446.dbc.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import cs446.dbc.models.BusinessCardModel
import cs446.dbc.models.EventModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CreateEditViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
): ViewModel() {
    // This class is literally just to hold these values when we are creating/saving edits to
    // business cards or events in their respect creation screens
    val createEditBusinessCard = savedStateHandle.getStateFlow("businessCard", BusinessCardModel())
    val createEditEvent = savedStateHandle.getStateFlow("eventCard", EventModel())
}