package cs446.dbc.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cs446.dbc.api.ApiFunctions
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
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.UUID
import javax.inject.Inject

// TODO: Could be renamed to app bar since we use this for the topAppBar (but could leave it if
// we want to add more stuff for both top bar, bottom bar, and general activity

@Serializable
data class Settings (
   val userId: String
)

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
    val userId = savedStateHandle.getStateFlow("userId", "")

    // TODO: Change these to Enums
    private val loadedMyCardsKey = "loadedMyCards"
    private val loadedSharedCardsKey = "loadedSharedCards"
    val loadedMyCards = savedStateHandle.getStateFlow(loadedMyCardsKey, false)
    val loadedSharedCards = savedStateHandle.getStateFlow(loadedSharedCardsKey, false)

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
        savedStateHandle[if (cardType == CardType.PERSONAL) myBusinessCardsContext else sharedBusinessCardsContext] = updatedList
        // Persist card to local storage
        saveCardToLocalStorage(card, context, directoryName)
    }

    private fun saveCardToLocalStorage(card: BusinessCardModel, context: Context, directoryName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val directory = context.getExternalFilesDir(directoryName)

                directory?.let {
                    if (!it.exists()) it.mkdirs()
                    val fileName = "Card_${card.id}.json"
                    val file = File(it, fileName)

                    file.writeText(Json.encodeToString(card))
                }
            } catch (e: Exception) {
                Log.e("AppViewModel", "Error saving card to local storage", e)
            }
        }
    }

    fun loadCardsFromDirectory(context: Context, directoryName: String, cardType: CardType): MutableList<BusinessCardModel> {
        return runBlocking {
            val job = viewModelScope.launch(Dispatchers.IO) {
                val directory = context.getExternalFilesDir(directoryName)

                directory?.let {
                    if (it.exists() && it.isDirectory) {
                        val cardFiles = it.listFiles() ?: return@launch
                        var loadedCards = cardFiles.mapNotNull { file ->
                            try {
                                val cardJSON = file.readText()
                                Json.decodeFromString<BusinessCardModel>(cardJSON)
                            } catch (e: Exception) {
                                Log.e(
                                    "AppViewModel",
                                    "Error reading or parsing card from file: ${file.name}",
                                    e
                                )
                                null
                            }
                        }.toMutableList()

                        loadedCards = loadedCards.filter { card -> card.cardType == cardType }
                            .toMutableList() // filter out only the type of cards we want


                        savedStateHandle[if (cardType == CardType.PERSONAL) "myBusinessCards" else "sharedBusinessCards"] =
                            loadedCards
                        savedStateHandle[if (cardType == CardType.PERSONAL) "loadedMyCards" else "loadedSharedCards"] =
                            true

                    } else {
                        Log.e("AppViewModel", "Error locating directory: $directoryName")
                    }
                }
            }
            job.join()

            val retCards =
                savedStateHandle.get<MutableList<BusinessCardModel>>(if (cardType == CardType.PERSONAL) "myBusinessCards" else "sharedBusinessCards")
                    ?: mutableListOf<BusinessCardModel>()
            retCards
        }
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

    fun updateUserId(newId: String) {
        savedStateHandle["userId"] = newId
    }

    private fun saveUserId(id: String, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val directory = context.getExternalFilesDir(null) ?: return@launch
                val file = File(directory, "Settings.json")
                file.writeText(Json.encodeToString(Settings(id)))
            } catch (e: Exception) {
                Log.e("AppViewModel", "Error saving userId to local storage", e)
            }
        }
    }

    fun loadUserId(context: Context) {
        return runBlocking {
            val job = viewModelScope.launch(Dispatchers.IO) {
                val directory = context.getExternalFilesDir(null) ?: return@launch
                directory.let {
                    try {
                        val files = it.listFiles() ?: return@launch
                        val filteredFiles = files.filter { file -> file.name == "Settings.json" }
                        if (filteredFiles.isNotEmpty()) {
                            val settingsFile = filteredFiles.first()
                            val settingsJson = settingsFile.readText()
                            val settings = Json.decodeFromString<Settings>(settingsJson)
                            savedStateHandle["userId"] = settings.userId
                        }
                    } catch (e: Exception) {
                        Log.e(
                            "AppViewModel",
                            "Error reading or parsing userId from Settings file",
                            e
                        )
                    }
                }
            }
            job.join()

            val updatedUserId = savedStateHandle.get<String>("userId")
            if (updatedUserId == "") {
                val newUserId = ApiFunctions.createUserId()
                updateUserId(newUserId)
                saveUserId(newUserId, context)
            }
        }
    }
}