package cs446.dbc.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// TODO: Could be renamed to app bar since we use this for the topAppBar (but could leave it if
// we want to add more stuff for both top bar, bottom bar, and general activity
class AppViewModel: ViewModel() {
    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    fun updateScreenTitle(newTitle: String) {
        _uiState.update { currentState ->
            currentState.copy(screenTitle = newTitle)
        }
    }
}