package cs446.dbc.viewmodels

import cs446.dbc.models.CardType
import cs446.dbc.models.Field
import java.util.UUID

sealed class BusinessCardAction {
    data class PopulateCard (val front: String, val back: String, val favorite: Boolean, val fields: MutableList<Field>, val cardType: CardType): BusinessCardAction()
    data class UpdateFront (val cardId: UUID, val front: String): BusinessCardAction()
    data class UpdateBack (val cardId: UUID, val back: String): BusinessCardAction()
    data class ToggleFavorite (val cardId: UUID): BusinessCardAction()
    data class InsertField (val cardId: UUID, val field: Field): BusinessCardAction()
    data class RemoveField (val cardId: UUID, val field: Field): BusinessCardAction()
    data class UpdateAllFields (val cardId: UUID, val fields: MutableList<Field>): BusinessCardAction()
    data class UpdateField (val cardId: UUID, val oldField: Field, val newField: Field): BusinessCardAction()
    data class UpdateCardType (val cardId: UUID, val newCardType: CardType): BusinessCardAction()
    data class UpdateCardContext (val newContext: CardType): BusinessCardAction()
}