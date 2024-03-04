package cs446.dbc.models

import java.util.UUID

data class BusinessCardModel(
    val id: UUID,
    val front: String,
    val back: String,
    var favorite: Boolean,
    val fields: MutableList<Field>
)

data class Field(
    val name: String,
    val value: String,
    val type: FieldType,
)

// TODO: Hyperlink fields to mail and web pages below
enum class FieldType {
    TEXT,
    URL, // open web page (company site, personal portfolio, etc.)
    EMAIL, // maito default mail app
    PHONE_NUMBER,
    GITHUB_USERNAME, // open Github Page
    LINKEDIN_ID // open LinkedIn Profile
}