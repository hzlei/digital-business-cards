package cs446.dbc.models

import kotlinx.serialization.Serializable

@Serializable
data class BusinessCardModel(
    val front: String,
    val back: String,
    val favorite: Boolean,
    val fields: MutableList<Field>
)

@Serializable
data class Field(
    val name: String,
    val value: String,
    val type: FieldType,
)

@Serializable
enum class FieldType {
    TEXT,
    URL,
    EMAIL,
    PHONE_NUMBER,
}