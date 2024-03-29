package cs446.dbc.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable


@Parcelize
@Serializable
data class EventModel(
    var id: String,
    var name: String,
    var location: String,
    // let's see how much time we have with the menu options, if we have time, we include dates,
    // otherwise we leave em out
    // TODO: make sure to convert the dates from strings to Date format
    var startDate: String,
    var endDate: String,
    var numUsers: Int,
    var maxUsers: Int = 1000,
    var maxUsersSet: Boolean = false,
    var eventType: EventType = EventType.HOSTED
) : Parcelable {
    constructor() : this(
        "",
        "",
        "",
        "",
        "",
        0,
        1000,
        false,
        EventType.HOSTED
    )
}

@Parcelize
@Serializable
enum class EventType : Parcelable {
    HOSTED,
    JOINED
}