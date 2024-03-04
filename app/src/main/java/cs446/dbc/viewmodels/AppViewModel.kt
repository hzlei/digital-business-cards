package cs446.dbc.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cs446.dbc.models.AppModel
import cs446.dbc.models.BusinessCardModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

// TODO: Could be renamed to app bar since we use this for the topAppBar (but could leave it if
// we want to add more stuff for both top bar, bottom bar, and general activity
class AppViewModel: ViewModel() {
    private val _uiState = MutableStateFlow(AppModel())
    val uiState: StateFlow<AppModel> = _uiState.asStateFlow()
    private val _cards = MutableStateFlow<List<BusinessCardModel>>(emptyList())
    val cards: StateFlow<List<BusinessCardModel>> = _cards.asStateFlow()

    fun saveCardToLocalStorage(card: BusinessCardModel, context: Context, directoryName: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val directory = File(context.filesDir, directoryName)
                    if (!directory.exists()) directory.mkdirs()

                    val fileName = "Card_${System.currentTimeMillis()}.json"
                    val file = File(directory, fileName)

                    file.writeText(Json.encodeToString(card))
                } catch (e: Exception) {
                    Log.e("AppViewModel", "Error saving card to local storage", e)
                }
            }
        }
    }

    fun loadCardsFromDirectory(context: Context, directoryName: String) {
        viewModelScope.launch {
            val directory = File(context.filesDir, directoryName)
            if (directory.exists() && directory.isDirectory) {
                val cardFiles = withContext(Dispatchers.IO) { directory.listFiles() } ?: return@launch

                cardFiles.forEach { file ->
                    try {
                        val cardJSON = withContext(Dispatchers.IO) { file.readText() }
                        val cardModel = Json.decodeFromString<BusinessCardModel>(cardJSON)
                        addCard(cardModel)
                    } catch (e: Exception) {
                        Log.e("AppViewModel", "Error reading or parsing card from file: ${file.name}", e)
                    }
                }
            }
        }
    }

    fun updateScreenTitle(newTitle: String) {
        _uiState.update { currentState ->
            currentState.copy(screenTitle = newTitle)
        }
    }

    fun addCard(card: BusinessCardModel) {
        val updatedCards = _cards.value + card
        _cards.value = updatedCards
    }
}