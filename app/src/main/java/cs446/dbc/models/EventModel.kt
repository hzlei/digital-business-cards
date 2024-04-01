package cs446.dbc.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import java.util.Date


@Parcelize
@Serializable
data class EventModel(
    var id: String,
    var name: String,
    var location: String,
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
        Date().time.toString(),
        (Date().time + 3 * 24 * 60 * 60 * 1000).toString(),
        0,
        1000,
        false,
        EventType.HOSTED
    )
}

@Parcelize
enum class EventType : Parcelable {
   HOSTED,
   JOINED
}