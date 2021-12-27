package blue.starry.stella.endpoints

import blue.starry.stella.models.PicModel
import blue.starry.stella.worker.MediaRegister
import blue.starry.stella.worker.StellaMongoDBPicCollection
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.locations.put
import io.ktor.response.*
import io.ktor.routing.Route
import org.bson.types.ObjectId
import org.litote.kmongo.eq
import org.litote.kmongo.id.toId

@Location("/pic/{id}/refresh")
data class PutPicRefresh(val id: String)

fun Route.putPicRefresh() {
    put<PutPicRefresh> { (id) ->
        val entry = StellaMongoDBPicCollection.findOne(PicModel::_id eq ObjectId(id).toId())
            ?: return@put call.respondApiError(HttpStatusCode.NotFound) {
                "Specified entry is not found."
            }

        if (!MediaRegister.registerByUrl(entry.url, false)) {
            call.respondApiError {
                "Unknown error occurred."
            }
        } else {
            val newEntry = StellaMongoDBPicCollection.findOne(PicModel::_id eq ObjectId(id).toId())!!

            call.respond(newEntry)
        }
    }
}
