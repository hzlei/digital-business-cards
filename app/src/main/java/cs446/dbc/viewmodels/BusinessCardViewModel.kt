package cs446.dbc.viewmodels

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import cs446.dbc.DBCApplication
import cs446.dbc.api.ApiFunctions
import cs446.dbc.bluetooth.BluetoothActionActivity
import cs446.dbc.models.BusinessCardModel
import cs446.dbc.models.CardType
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class BusinessCardViewModel @Inject constructor(
    application: Application,
    private val savedStateHandle: SavedStateHandle,
    private val viewModelContext: CardType,
    private val appContext: Context
): AndroidViewModel(application) {
    private val myBusinessCardsContext = "myBusinessCards"
    private val sharedBusinessCardsContext = "sharedBusinessCards"

    // TODO: How will we handle injection of saved preferences??
    val myBusinessCards = savedStateHandle.getStateFlow(myBusinessCardsContext, mutableListOf<BusinessCardModel>())
    val sharedBusinessCards = savedStateHandle.getStateFlow(sharedBusinessCardsContext, mutableListOf<BusinessCardModel>())

    private val currContext = savedStateHandle.getStateFlow("cardContext", if (viewModelContext == CardType.PERSONAL) myBusinessCardsContext else sharedBusinessCardsContext)

    var sharedCardsSnapshotList: SnapshotStateList<BusinessCardModel>? = null
    var businssCardSnapshotList: SnapshotStateList<BusinessCardModel>? = null

    val currCardViewId = savedStateHandle.getStateFlow("currCardViewId", "")


    // TODO: Do we need a separate remove card action when removing the card?
    fun performAction(action: BusinessCardAction) {
        when (action) {
            is BusinessCardAction.ToggleFavorite -> toggleFavorite(action.cardId)
            is BusinessCardAction.InsertField -> TODO()
            is BusinessCardAction.PopulateCard -> populateCard(action)
            is BusinessCardAction.InsertCard -> insertCard(action)
            is BusinessCardAction.InsertCards -> insertCards(action)
            is BusinessCardAction.RemoveCard -> removeCard(action)
            is BusinessCardAction.UpdateCard -> updateCard(action)
            is BusinessCardAction.RemoveField -> TODO()
            is BusinessCardAction.UpdateAllFields -> TODO()
            is BusinessCardAction.UpdateBack -> TODO()
            is BusinessCardAction.UpdateField -> TODO()
            is BusinessCardAction.UpdateFront -> TODO()
            is BusinessCardAction.UpdateCardType -> TODO()
            is BusinessCardAction.UpdateCardContext -> updateCardContext(action.newContext)
            is BusinessCardAction.SetCardEditFocus -> changeCurrCardViewId(action.cardId)
            is BusinessCardAction.ShareCardBluetooth -> shareBluetoothCard(action)
            is BusinessCardAction.ReceiveCardsBluetooth -> receiveBluetoothCards()
            is BusinessCardAction.RequestCard -> requestCard(action.card, action.appViewModel)
            else -> TODO() // not actually, this is just to shut up the error
        }
    }

    private fun updateCardContext(newContext: CardType) {
        savedStateHandle["cardContext"] = if (newContext == CardType.PERSONAL) myBusinessCardsContext else sharedBusinessCardsContext
    }

    private fun toggleFavorite(cardId: String) {
        val cardList = savedStateHandle.get<MutableList<BusinessCardModel>>(currContext.value) ?: return
        var updatedList = cardList.map { if (it.id == cardId) it.copy(favorite = !it.favorite) else it }.toMutableList()
        updatedList = updatedList.sortedWith(compareBy({ !it.favorite }, { it.front})).toMutableList()
        savedStateHandle[currContext.value] = updatedList
        businssCardSnapshotList?.clear()
        businssCardSnapshotList?.addAll(updatedList)
    }

    fun updateCardContext(ctx: String) {
        savedStateHandle["cardContext"] = if (ctx == "myBusinessCards") myBusinessCardsContext else sharedBusinessCardsContext
    }

    private fun populateCard(action: BusinessCardAction.PopulateCard) {
        val card = BusinessCardModel(
            id = UUID.randomUUID().toString(),
            front = action.front,
            back = action.back,
            favorite = action.favorite,
            fields = action.fields,
            cardType = action.cardType
        )
        insertCard(BusinessCardAction.InsertCard(card))
    }

    private fun insertCard(action: BusinessCardAction.InsertCard) {
        val ctx = currContext.value
        val currCards = savedStateHandle.get<MutableList<BusinessCardModel>>(ctx)
        currCards?.add(action.card)
        currCards?.sortWith(compareBy({ !it.favorite }, { it.front}))
        savedStateHandle[currContext.value] = currCards
        if (ctx == myBusinessCardsContext) {
            businssCardSnapshotList?.clear()
            businssCardSnapshotList?.addAll(currCards!!)
        }
        else {
            sharedCardsSnapshotList?.clear()
            sharedCardsSnapshotList?.addAll(currCards!!)
        }
    }

    private fun insertCards(action: BusinessCardAction.InsertCards) {
        val ctx = currContext.value
        val cards = savedStateHandle.get<MutableList<BusinessCardModel>>(ctx)
        cards?.addAll(action.cards)
        cards?.sortWith(compareBy({ !it.favorite }, { it.front}))
        savedStateHandle[currContext.value] = cards
        if (ctx == myBusinessCardsContext) {
            businssCardSnapshotList?.clear()
            businssCardSnapshotList?.addAll(cards!!)
        }
        else {
            sharedCardsSnapshotList?.clear()
            sharedCardsSnapshotList?.addAll(cards!!)
        }

    }

    private fun removeCard(action: BusinessCardAction.RemoveCard) {
        val ctx = currContext.value
        val cards = savedStateHandle.get<MutableList<BusinessCardModel>>(ctx)
        cards?.removeIf { it.id == action.card.id }
        savedStateHandle[currContext.value] = cards
        if (ctx == myBusinessCardsContext) {
            businssCardSnapshotList?.clear()
            businssCardSnapshotList?.addAll(cards!!)
        }
        else {
            sharedCardsSnapshotList?.clear()
            sharedCardsSnapshotList?.addAll(cards!!)
        }
        // TODO: Delete from local storage as well
        action.appViewModel.deleteCardFromLocalStorage(action.card, appContext, "businessCards")
    }

    private fun updateCard(action: BusinessCardAction.UpdateCard) {
        val ctx = currContext.value
        val cards = savedStateHandle.get<MutableList<BusinessCardModel>>(ctx)
        cards?.removeIf { it.id == action.cardID }
        cards?.add(action.card)
        cards?.sortWith(compareBy({ !it.favorite }, { it.front}))
        savedStateHandle[currContext.value] = cards
        businssCardSnapshotList?.clear()
        businssCardSnapshotList?.addAll(cards!!)
    }

    fun changeCurrCardViewId (id: String?) {
        savedStateHandle["currCardViewId"] = id
    }
    private fun requestCard(newCard: BusinessCardModel, appViewModel: AppViewModel) {
        if (newCard.front != "") {
            // download image
            ApiFunctions.downloadImage(newCard.front, "businessCards", appContext)
        }
        if (newCard.back != "") {
            // download image
            ApiFunctions.downloadImage(newCard.front, "businessCards", appContext)
        }
        // put card into shared cards list, and save to local storage
        val sharedCardsList = savedStateHandle.get<MutableList<BusinessCardModel>>(sharedBusinessCardsContext)!!
        sharedCardsList.add(newCard)
        sharedCardsList.sortWith(compareBy({ !it.favorite }, { it.front}))
        appViewModel.saveCardToLocalStorage(newCard, appContext, "businessCards")
    }

    private fun shareBluetoothCard(action : BusinessCardAction.ShareCardBluetooth) {
        val app = getApplication<Application>()
        app.startActivity(Intent(app, BluetoothActionActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra("outCard", action.card)
        })
    }

    private fun receiveBluetoothCards() {
        val app = getApplication<Application>()
        app.startActivity(Intent(app, BluetoothActionActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }
}