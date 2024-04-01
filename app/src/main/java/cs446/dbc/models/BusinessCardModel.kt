package cs446.dbc.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class BusinessCardModel(
    val id: String,
    var front: String,
    var back: String,
    var favorite: Boolean,
    val fields: MutableList<Field>,
    var template: TemplateType = TemplateType.DEFAULT,
    var cardType: CardType = CardType.PERSONAL, // TODO: figure out how card types will change during sharing
    val eventId: String = "",
    val eventUserId: String = ""
) : Parcelable {
    constructor() : this(
        "",
        "",
        "",
        false,
        mutableListOf<Field>(),
        TemplateType.DEFAULT,
        CardType.PERSONAL,
        "",
        ""
    )
}

@Parcelize
@Serializable
data class Field(
    var name: String,
    var value: String,
    val type: FieldType,
) : Parcelable

// TODO: Hyperlink fields to mail and web pages below

@Parcelize
@Serializable
enum class FieldType : Parcelable {
    TEXT,
    URL, // open web page (company site, personal portfolio, etc.)
    EMAIL, // maito default mail app
    PHONE_NUMBER,
    GITHUB_USERNAME, // open Github Page
    LINKEDIN_ID // open LinkedIn Profile
}

@Parcelize
@Serializable
enum class CardType : Parcelable {
    PERSONAL, // representing cards in my cards screen
    SHARED,
    EVENT_VIEW
}

@Parcelize
@Serializable
enum class TemplateType : Parcelable {
    DEFAULT,
    TEMPLATE_1,
    TEMPLATE_2,
    TEMPLATE_3,
    CUSTOM,
    EVENT_VIEW_TEMPLATE
}