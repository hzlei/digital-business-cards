package cs446.dbc.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider

class BusinessCardViewModelFactory(
    private val savedStateHandle: SavedStateHandle
): ViewModelProvider.Factory {
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(BusinessCardViewModel::class.java)) {
//            @Suppress("UNCHECKED_CAST")
//            return BusinessCardViewModel(savedStateHandle) as T
//        }
//        throw IllegalArgumentException("Unknown ViewModel class")
//    }
}