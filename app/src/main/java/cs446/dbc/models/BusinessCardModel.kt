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

enum class FieldType {
    TEXT,
    URL,
    EMAIL,
    PHONE_NUMBER,
}