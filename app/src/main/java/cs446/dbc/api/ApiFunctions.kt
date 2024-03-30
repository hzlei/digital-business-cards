package cs446.dbc.api

import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FileDataPart
import com.github.kittinunf.fuel.coroutines.awaitStringResponseResult
import cs446.dbc.models.BusinessCardModel
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File


data class EventExists(
    val exists: Boolean
)

object ApiFunctions {
    private const val serverUrl: String = "https://digital-business-cards.fly.dev/api"
    private const val apiKey: String = "idVqnP2UV0KYyu510aztMo8lwI9QH72d"
    private const val apiKeyParam: String = "key=$apiKey"

    fun createUserId(): String {
        return runBlocking {
            val (_, _, result) = Fuel.post("$serverUrl/user?$apiKeyParam").awaitStringResponseResult()
            return@runBlocking result.get()
        }
    }

    fun createEvent(): String {
        return runBlocking {
            val (_, _, result) = Fuel.post("$serverUrl/event?$apiKeyParam").awaitStringResponseResult()
            // returns event id
            return@runBlocking result.get()
        }
    }

    fun checkEventExists(eventId: String): Boolean {
        return runBlocking {
            val (_, _, result) = Fuel.get("$serverUrl/event/$eventId/exists?$apiKeyParam").awaitStringResponseResult()
            val data = Json.decodeFromString<EventExists>(result.get())
            return@runBlocking data.exists
        }
    }

    fun addEventCard(card: BusinessCardModel, eventId: String): String {
        return runBlocking {
            val (_, _, result) = Fuel.post("$serverUrl/event/$eventId/card?$apiKeyParam").awaitStringResponseResult()
            return@runBlocking result.get()
        }
    }

    fun getAllEventCards(eventId: String): MutableList<BusinessCardModel> {
        return runBlocking {
            val (_, _, result) = Fuel.get("$serverUrl/event/$eventId/card?$apiKeyParam").awaitStringResponseResult()
            return@runBlocking Json.decodeFromString<MutableList<BusinessCardModel>>(result.get())
        }
    }

    fun deleteEvent(eventId: String): Boolean {
        return runBlocking {
            val (_, response, _) = Fuel.delete("$serverUrl/event/$eventId?$apiKeyParam").awaitStringResponseResult()
            return@runBlocking response.statusCode == 200
        }
    }


    fun saveUserCard(card: BusinessCardModel, userId: String): Boolean {
        return runBlocking {
            val body = Json.encodeToString(card)
            val (_, response, _) = Fuel.post("$serverUrl/user/$userId/card?$apiKeyParam")
                .body(body)
                .awaitStringResponseResult()
            return@runBlocking response.statusCode == 200
        }
    }

    // TODO: Remember to update the image path to this value for the card
    // TODO: ensure saveUserCard is called first before this function is called
    fun uploadImage(imagePath: String, cardSide: String, userId: String, cardId: String): String {
        return runBlocking {
            val file = FileDataPart.from(imagePath, name = "image")
            val (_, _, result) = Fuel.upload("$serverUrl/user/$userId/card/$cardId/image/$cardSide?$apiKeyParam")
                .add(file)
                .awaitStringResponseResult()
            return@runBlocking result.get()
        }
    }

    fun downloadImage(imagePath: String, directory: String) {
        // Assume imagePath will be the server path string
        return runBlocking {
            val path = imagePath.replace("_","/")
            val file =  File("$directory/$imagePath")
            Fuel.download("$serverUrl/$path").fileDestination { _, _ ->
                file
            }.response { req, res, result ->
                val (data, error) = result
                if (error != null) {
                    Log.e("fetchProfileImage", "error: ${error}")
                } else {
                    result.fold({ bytes ->
                        Log.e("fetchProfileImage", "file bytes --> ${file.length()}, response bytes -> ${bytes.size}")
                    }, { err ->
                        Log.e("fetchProfileImage", "error: $err")
                    })
                }
            }
        }
    }

}