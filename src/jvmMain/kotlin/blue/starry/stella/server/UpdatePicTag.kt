package blue.starry.stella.server

import blue.starry.stella.Stella
import blue.starry.stella.models.PicEntry
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.Route
import org.bson.types.ObjectId
import org.litote.kmongo.*
import org.litote.kmongo.id.toId
import java.time.Instant

@Location("/pic/{id}/tag")
data class EditTag(val id: String)

fun Route.putPicTag() {
    put<EditTag> { param ->
        val tag = call.receiveParameters()["tag"] ?: return@put
        val oldEntry = Stella.PicCollection.findOne(PicEntry::_id eq ObjectId(param.id).toId()) ?: return@put call.respondApiError(HttpStatusCode.NotFound) {
            "Specified entry is not found."
        }

        if (tag in oldEntry.tags.map { it.value }) {
            return@put call.respondApiError(HttpStatusCode.BadRequest) {
                "Input tag already exists in database."
            }
        }
        val updates = combine(
            addToSet(
                PicEntry::tags, PicEntry.Tag(
                    value = tag, user = call.request.origin.remoteHost, locked = false
                )
            ), setValue(PicEntry::timestamp / PicEntry.Timestamp::manual_updated, Instant.now().toEpochMilli())
        )
        Stella.PicCollection.updateOne(PicEntry::_id eq ObjectId(param.id).toId(), updates)
        val entry = Stella.PicCollection.findOne(PicEntry::_id eq ObjectId(param.id).toId())!!
        call.respond(entry)

        Stella.Logger.info {
            "${entry.url} のエントリが更新されました。「$tag」が追加されました。(${call.request.origin.remoteHost})"
        }
    }
}

fun Route.deletePicTag() {
    delete<EditTag> { param ->
        val tag = call.receiveParameters()["tag"] ?: return@delete
        val oldEntry = Stella.PicCollection.findOne(PicEntry::_id eq ObjectId(param.id).toId()) ?: return@delete call.respondApiError(HttpStatusCode.NotFound) {
            "Specified entry is not found."
        }

        if (tag !in oldEntry.tags.map { it.value }) {
            return@delete call.respondApiError(HttpStatusCode.BadRequest) {
                "Input tag is not found in database."
            }
        }

        if (tag in oldEntry.tags.filter { it.locked }.map { it.value }) {
            return@delete call.respondApiError(HttpStatusCode.BadRequest) {
                "Input tag is locked in database."
            }
        }
        val tags = oldEntry.tags.filter { it.value != tag }
        val updates = combine(
            setValue(PicEntry::tags, tags), setValue(PicEntry::timestamp / PicEntry.Timestamp::manual_updated, Instant.now().toEpochMilli())
        )
        Stella.PicCollection.updateOne(PicEntry::_id eq ObjectId(param.id).toId(), updates)
        val entry = Stella.PicCollection.findOne(PicEntry::_id eq ObjectId(param.id).toId())!!
        call.respond(entry)

        Stella.Logger.info {
            "${entry.url} のエントリが更新されました。「$tag」が削除されました。(${call.request.origin.remoteHost})"
        }
    }
}
