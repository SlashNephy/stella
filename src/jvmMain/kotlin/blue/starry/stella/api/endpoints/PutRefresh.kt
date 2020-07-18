package blue.starry.stella.api.endpoints

import blue.starry.stella.api.respondApi
import blue.starry.stella.api.respondApiError
import blue.starry.stella.api.serialize
import blue.starry.stella.api.toPic
import blue.starry.stella.collection
import blue.starry.stella.worker.MediaRegister
import com.mongodb.client.model.Filters
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.Location
import io.ktor.locations.put
import io.ktor.routing.Route
import org.bson.types.ObjectId

@Location("/refresh/{id}")
data class PutRefresh(val id: String)

fun Route.putRefresh() {
    put<PutRefresh> { (id) ->
        val entry = collection.findOne(Filters.eq("_id", ObjectId(id)))?.toPic()
        if (entry == null) {
            call.respondApiError(HttpStatusCode.NotFound) {
                "Specified entry is not found."
            }

            return@put
        }

        if (!MediaRegister.registerByUrl(entry.url, null, false)) {
            call.respondApiError {
                "Unknown error occurred."
            }
        } else {
            val newEntry = collection.findOne(Filters.eq("_id", ObjectId(id)))!!

            call.respondApi {
                newEntry.serialize()
            }
        }
    }
}
