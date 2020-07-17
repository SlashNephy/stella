package blue.starry.stella.api.endpoints

import blue.starry.stella.api.respondApi
import blue.starry.stella.api.respondApiError
import blue.starry.stella.api.serialize
import blue.starry.stella.api.toPic
import blue.starry.stella.collection
import blue.starry.stella.logger
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import io.ktor.application.call
import io.ktor.features.origin
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.delete
import io.ktor.locations.put
import io.ktor.request.receiveParameters
import io.ktor.routing.Route
import org.bson.types.ObjectId
import java.util.*

@KtorExperimentalLocationsAPI
@Location("/edit/{id}/tag")
data class EditTag(val id: String)

@KtorExperimentalLocationsAPI
fun Route.putEditTag() {
    put<EditTag> { (id) ->
        val tag = call.receiveParameters()["tag"]
        if (tag == null) {
            call.respondApiError(HttpStatusCode.BadRequest) {
                "Essential parameter \"tag\" is invalid or not set."
            }

            return@put
        }

        val oldEntry = collection.findOne(Filters.eq("_id", ObjectId(id)))?.toPic()
        if (oldEntry == null) {
            call.respondApiError(HttpStatusCode.NotFound) {
                "Specified entry is not found."
            }

            return@put
        }

        if (tag in oldEntry.tags.map { it.value }) {
            call.respondApiError(HttpStatusCode.BadRequest) {
                "Input tag already exists in database."
            }

            return@put
        }

        val updates = Updates.combine(
            Updates.addToSet("tags", mapOf(
                "value" to tag,
                "user" to call.request.origin.remoteHost,
                "locked" to false
            )),
            Updates.set("timestamp.manual_updated", Calendar.getInstance().timeInMillis)
        )
        collection.updateOne(Filters.eq("_id", ObjectId(id)), updates)

        val entry = collection.findOne(Filters.eq("_id", ObjectId(id)))!!
        call.respondApi {
            entry.serialize()
        }

        logger.info {
            val pic = entry.toPic()
            "${pic.url} のエントリが更新されました。「$tag」が追加されました。(${call.request.origin.remoteHost})"
        }
    }
}

@KtorExperimentalLocationsAPI
fun Route.deleteEditTag() {
    delete<EditTag> { (id) ->
        val tag = call.receiveParameters()["tag"]
        if (tag == null) {
            call.respondApiError(HttpStatusCode.BadRequest) {
                "Essential parameter \"tag\" is invalid or not set."
            }

            return@delete
        }

        val oldEntry = collection.findOne(Filters.eq("_id", ObjectId(id)))?.toPic()
        if (oldEntry == null) {
            call.respondApiError(HttpStatusCode.NotFound) {
                "Specified entry is not found."
            }

            return@delete
        }

        if (tag !in oldEntry.tags.map { it.value }) {
            call.respondApiError(HttpStatusCode.BadRequest) {
                "Input tag is not found in database."
            }

            return@delete
        }

        if (tag in oldEntry.tags.filter { it.locked }.map { it.value }) {
            call.respondApiError(HttpStatusCode.BadRequest) {
                "Input tag is locked in database."
            }

            return@delete
        }

        val tags = oldEntry.tags.filter { it.value != tag }.map {
            mapOf(
                "value" to it.value,
                "user" to it.user,
                "locked" to it.locked
            )
        }

        val updates = Updates.combine(
            Updates.set("tags", tags),
            Updates.set("timestamp.manual_updated", Calendar.getInstance().timeInMillis)
        )
        collection.updateOne(Filters.eq("_id", ObjectId(id)), updates)

        val entry = collection.findOne(Filters.eq("_id", ObjectId(id)))!!
        call.respondApi {
            entry.serialize()
        }

        logger.info {
            val pic = entry.toPic()
            "${pic.url} のエントリが更新されました。「$tag」が削除されました。(${call.request.origin.remoteHost})"
        }
    }
}
