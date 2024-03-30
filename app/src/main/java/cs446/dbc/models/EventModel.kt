package cs446.dbc.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable


@Parcelize
@Serializable
data class EventModel(
    val id: String,
    val name: String,
    val location: String,
    // let's see how much time we have with the menu options, if we have time, we include dates,
    // otherwise we leave em out
//    val startDate: String,
//    val endDate: String,
    val numUsers: Int,
    val maxUsers: Int = Int.MAX_VALUE,
    val eventType: EventType = EventType.HOSTED
) : Parcelable

@Parcelize
@Serializable
enum class EventType : Parcelable {
    HOSTED,
    JOINED
}