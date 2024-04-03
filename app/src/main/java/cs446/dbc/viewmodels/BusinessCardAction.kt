package cs446.dbc.viewmodels

import android.content.Context
import cs446.dbc.models.BusinessCardModel
import cs446.dbc.models.CardType
import cs446.dbc.models.Field

sealed class BusinessCardAction {
    data class PopulateCard (val front: String, val back: String, val favorite: Boolean, val fields: MutableList<Field>, val cardType: CardType, val appViewModel: AppViewModel): BusinessCardAction()
    data class InsertCard (val card: BusinessCardModel, val appViewModel: AppViewModel): BusinessCardAction()
    data class InsertCards(val cards: MutableList<BusinessCardModel>, val appViewModel: AppViewModel): BusinessCardAction()
    data class RemoveCard(val card: BusinessCardModel, val appViewModel: AppViewModel): BusinessCardAction()
    data class UpdateCard(val cardID: String, val card: BusinessCardModel, val appViewModel: AppViewModel): BusinessCardAction()
    data class UpdateFront (val cardId: String, val front: String): BusinessCardAction()
    data class UpdateBack (val cardId: String, val back: String): BusinessCardAction()
    data class ToggleFavorite (val cardId: String): BusinessCardAction()
    data class InsertField (val cardId: String, val field: Field): BusinessCardAction()
    data class RemoveField (val cardId: String, val field: Field): BusinessCardAction()
    data class UpdateAllFields (val cardId: String, val fields: MutableList<Field>): BusinessCardAction()
    data class UpdateField (val cardId: String, val oldField: Field, val newField: Field): BusinessCardAction()
    data class UpdateCardType (val cardId: String, val newCardType: CardType): BusinessCardAction()
    data class UpdateCardContext (val newContext: CardType): BusinessCardAction()
    data class SetCardEditFocus(val cardId: String): BusinessCardAction()
    data class RequestCard(val card: BusinessCardModel, val appViewModel: AppViewModel): BusinessCardAction()
    data class ShareCardBluetooth (val card: BusinessCardModel): BusinessCardAction()
    data class ReceiveCardsBluetooth(val stub : Boolean = false): BusinessCardAction()
}