package cs446.dbc.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cs446.dbc.models.AppModel
import cs446.dbc.models.BusinessCardModel
import cs446.dbc.models.CardType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.UUID
import javax.inject.Inject

// TODO: Could be renamed to app bar since we use this for the topAppBar (but could leave it if
// we want to add more stuff for both top bar, bottom bar, and general activity
@HiltViewModel
class AppViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val viewModelContext: CardType
): ViewModel() {

    private val myBusinessCardsContext = "myBusinessCards"
    private val sharedBusinessCardsContext = "sharedBusinessCards"

    // TODO: change this to savedstate
    private val _uiState = MutableStateFlow(AppModel())
    val uiState: StateFlow<AppModel> = _uiState.asStateFlow()

    // duplicate logic, we should aim to have this together
    private val currContext = savedStateHandle.getStateFlow("cardContext", if (viewModelContext == CardType.PERSONAL) myBusinessCardsContext else sharedBusinessCardsContext)

    val myCards = savedStateHandle.getStateFlow(myBusinessCardsContext, mutableListOf<BusinessCardModel>())
    val sharedCards = savedStateHandle.getStateFlow(sharedBusinessCardsContext, mutableListOf<BusinessCardModel>())

    // TODO: Change these to Enums
    private val loadedMyCardsKey = "loadedMyCards"
    private val loadedSavedCardsKey = "loadedSavedCards"
    val loadedMyCards = savedStateHandle.getStateFlow(loadedMyCardsKey, false)
    val loadedSharedCards = savedStateHandle.getStateFlow(loadedSavedCardsKey, false)

    private fun updateCardContext(newContext: CardType) {
        savedStateHandle["cardContext"] = if (newContext == CardType.PERSONAL) myBusinessCardsContext else sharedBusinessCardsContext
    }

    fun getMyCards(): MutableList<BusinessCardModel>{
        return myCards.value
    }

    fun getSharedCards(): MutableList<BusinessCardModel>{
        return sharedCards.value
    }

//    fun addCard(card: BusinessCardModel, context: Context, directoryName: String = "businessCards"): MutableList<BusinessCardModel>{
    fun addCard(card: BusinessCardModel, context: Context, directoryName: String = "businessCards", cardType: CardType) {
//        val ctx = savedStateHandle.get<String>("cardContext")!!
//        val updatedCards = savedStateHandle.get<MutableList<BusinessCardModel>>(ctx)!!.add(card)
//        savedStateHandle[ctx] = updatedCards
//        saveCardToLocalStorage(card, context, directoryName)
//
//        return if (ctx == myBusinessCardsContext) getMyCards() else getSharedCards()
        val cardList = if (cardType == CardType.PERSONAL) myCards else sharedCards
        val updatedList = cardList.value.toMutableList().apply {
            add(card)
        }
        // Save updated list back to state handle
        savedStateHandle[if (cardType == CardType.PERSONAL) "myBusinessCards" else "sharedBusinessCards"] = updatedList
        // Persist card to local storage
        saveCardToLocalStorage(card, context, directoryName)
    }

    private fun saveCardToLocalStorage(card: BusinessCardModel, context: Context, directoryName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val directory = File(context.filesDir, directoryName)
                if (!directory.exists()) directory.mkdirs()
                val fileName = "Card_${UUID.randomUUID()}.json"
                val file = File(directory, fileName)

                file.writeText(Json.encodeToString(card))
            } catch (e: Exception) {
                Log.e("AppViewModel", "Error saving card to local storage", e)
            }
        }
    }

    fun loadCardsFromDirectory(context: Context, directoryName: String, cardType: CardType): MutableList<BusinessCardModel> {
        viewModelScope.launch(Dispatchers.IO) {
            val directory = File(context.filesDir, directoryName)
            if (directory.exists() && directory.isDirectory) {
                val cardFiles = directory.listFiles() ?: return@launch

                val loadedCards = cardFiles.mapNotNull { file ->
                    try {
                        val cardJSON = file.readText()
                        Json.decodeFromString<BusinessCardModel>(cardJSON)
                    } catch (e: Exception) {
                        Log.e("AppViewModel", "Error reading or parsing card from file: ${file.name}", e)
                        null
                    }
                }.toMutableList()

                savedStateHandle[if (cardType == CardType.PERSONAL) "myBusinessCards" else "sharedBusinessCards"] = loadedCards
            }
        }
        savedStateHandle[if (cardType == CardType.PERSONAL) loadedMyCardsKey else loadedSavedCardsKey] = true
        return savedStateHandle[if (cardType == CardType.PERSONAL) "myBusinessCards" else "sharedBusinessCards"]!!
    }

    fun updateLoadedMyCards(hasLoaded: Boolean) {
        savedStateHandle["loadedMyCards"] = hasLoaded
    }

    fun updateLoadedSharedCards(hasLoaded: Boolean) {
        savedStateHandle["loadedSharedCards"] = hasLoaded
    }

    fun updateScreenTitle(newTitle: String) {
        _uiState.update { currentState ->
            currentState.copy(screenTitle = newTitle)
        }
    }
}