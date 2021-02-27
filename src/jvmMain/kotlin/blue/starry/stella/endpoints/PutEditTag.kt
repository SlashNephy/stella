package blue.starry.stella.endpoints

import blue.starry.stella.logger
import blue.starry.stella.models.PicModel
import blue.starry.stella.worker.StellaMongoDBPicCollection
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.Route
import org.bson.types.ObjectId
import org.litote.kmongo.eq
import org.litote.kmongo.toId
import java.util.*

@Location("/edit/{id}/tag")
data class PutEditTag(val id: String)

fun Route.putEditTag() {
    put<PutEditTag> { (id) ->
        val tag = call.receiveParameters()["tag"]
        if (tag == null) {
            call.respondApiError(HttpStatusCode.BadRequest) {
                "Essential parameter \"tag\" is invalid or not set."
            }

            return@put
        }

        val oldEntry = StellaMongoDBPicCollection.findOne(PicModel::_id eq id.toId())
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
        StellaMongoDBPicCollection.updateOne(PicModel::_id eq id.toId(), updates)

        val entry = StellaMongoDBPicCollection.findOne(Filters.eq("_id", ObjectId(id)))!!
        call.respond(entry)

        logger.info {
            "${entry.url} のエントリが更新されました。「$tag」が追加されました。(${call.request.origin.remoteHost})"
        }
    }
}

fun Route.deleteEditTag() {
    delete<PutEditTag> { (id) ->
        val tag = call.receiveParameters()["tag"]
        if (tag == null) {
            call.respondApiError(HttpStatusCode.BadRequest) {
                "Essential parameter \"tag\" is invalid or not set."
            }

            return@delete
        }

        val oldEntry = StellaMongoDBPicCollection.findOne(PicModel::_id eq id.toId())
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
        StellaMongoDBPicCollection.updateOne(PicModel::_id eq id.toId(), updates)

        val entry = StellaMongoDBPicCollection.findOne(PicModel::_id eq id.toId())!!
        call.respond(entry)

        logger.info {
            "${entry.url} のエントリが更新されました。「$tag」が削除されました。(${call.request.origin.remoteHost})"
        }
    }
}
