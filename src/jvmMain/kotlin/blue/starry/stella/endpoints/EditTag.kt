package blue.starry.stella.endpoints

import blue.starry.stella.logger
import blue.starry.stella.models.PicModel
import blue.starry.stella.worker.StellaMongoDBPicCollection
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.Route
import org.litote.kmongo.*
import java.time.Instant
import java.util.*

@Location("/edit/{id}/tag")
data class EditTag(val id: String, val tag: String)

fun Route.putEditTag() {
    put<EditTag> { param ->
        val oldEntry = StellaMongoDBPicCollection.findOne(PicModel::_id eq param.id.toId())
            ?: return@put call.respondApiError(HttpStatusCode.NotFound) {
                "Specified entry is not found."
            }

        if (param.tag in oldEntry.tags.map { it.value }) {
            return@put call.respondApiError(HttpStatusCode.BadRequest) {
                "Input tag already exists in database."
            }
        }

        val updates = combine(
            addToSet(PicModel::tags, PicModel.Tag(
                value = param.tag,
                user = call.request.origin.remoteHost,
                locked = false
            )),
            setValue(PicModel::timestamp / PicModel.Timestamp::manual_updated, Instant.now().toEpochMilli())
        )
        StellaMongoDBPicCollection.updateOne(PicModel::_id eq param.id.toId(), updates)

        val entry = StellaMongoDBPicCollection.findOne(PicModel::_id eq param.id.toId())!!
        call.respond(entry)

        logger.info {
            "${entry.url} のエントリが更新されました。「${param.tag}」が追加されました。(${call.request.origin.remoteHost})"
        }
    }
}

fun Route.deleteEditTag() {
    delete<EditTag> { param ->
        val oldEntry = StellaMongoDBPicCollection.findOne(PicModel::_id eq param.id.toId())
            ?: return@delete call.respondApiError(HttpStatusCode.NotFound) {
                "Specified entry is not found."
            }

        if (param.tag !in oldEntry.tags.map { it.value }) {
            return@delete call.respondApiError(HttpStatusCode.BadRequest) {
                "Input tag is not found in database."
            }
        }

        if (param.tag in oldEntry.tags.filter { it.locked }.map { it.value }) {
            return@delete call.respondApiError(HttpStatusCode.BadRequest) {
                "Input tag is locked in database."
            }
        }

        val tags = oldEntry.tags.filter { it.value != param.tag }
        val updates = combine(
            setValue(PicModel::tags, tags),
            setValue(PicModel::timestamp / PicModel.Timestamp::manual_updated, Instant.now().toEpochMilli())
        )
        StellaMongoDBPicCollection.updateOne(PicModel::_id eq param.id.toId(), updates)

        val entry = StellaMongoDBPicCollection.findOne(PicModel::_id eq param.id.toId())!!
        call.respond(entry)

        logger.info {
            "${entry.url} のエントリが更新されました。「${param.tag}」が削除されました。(${call.request.origin.remoteHost})"
        }
    }
}
