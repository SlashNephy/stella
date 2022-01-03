package blue.starry.stella.server.endpoints

import blue.starry.stella.Stella
import blue.starry.stella.models.PicEntry
import blue.starry.stella.server.respondApiError
import blue.starry.stella.worker.BatchUpdater
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.Location
import io.ktor.locations.put
import io.ktor.response.respond
import io.ktor.routing.Route
import org.bson.types.ObjectId
import org.litote.kmongo.eq
import org.litote.kmongo.id.toId

@Location("/pic/{id}/refresh")
data class PutPicRefresh(val id: String)

fun Route.putPicRefresh() {
    put<PutPicRefresh> { param ->
        val filter = PicEntry::_id eq ObjectId(param.id).toId()
        val entry = Stella.PicCollection.findOne(filter)
            ?: return@put call.respondApiError(HttpStatusCode.NotFound) {
                "Specified entry was not found."
            }

        if (BatchUpdater.updateOne(entry, false)) {
            val newEntry = Stella.PicCollection.findOne(filter)!!
            call.respond(newEntry)
        } else {
            call.respondApiError {
                "Unknown error occurred."
            }
        }
    }
}
