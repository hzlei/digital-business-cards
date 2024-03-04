package cs446.dbc.models

data class BusinessCardModel(
    val front: String,
    val back: String,
    val favorite: Boolean,
    val fields: MutableList<Field>
)

data class Field(
    val name: String,
    val value: String,
    val type: FieldType,
)

enum class FieldType {
    TEXT,
    URL,
    EMAIL,
    PHONE_NUMBER,
}