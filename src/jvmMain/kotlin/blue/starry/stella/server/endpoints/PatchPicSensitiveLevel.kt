package blue.starry.stella.server.endpoints

import blue.starry.stella.Stella
import blue.starry.stella.models.PicEntry
import blue.starry.stella.models.internal.SensitiveLevelSerializer
import blue.starry.stella.server.respondApiError
import com.mongodb.client.model.Updates
import io.ktor.application.call
import io.ktor.features.origin
import io.ktor.http.HttpStatusCode
import io.ktor.locations.Location
import io.ktor.locations.patch
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.routing.Route
import org.bson.types.ObjectId
import org.litote.kmongo.combine
import org.litote.kmongo.div
import org.litote.kmongo.eq
import org.litote.kmongo.id.toId
import org.litote.kmongo.setValue
import java.time.Instant

@Location("/pic/{id}/sensitive_level")
data class PatchPicSensitiveLevel(val id: String)

fun Route.patchPicSensitiveLevel() {
    patch<PatchPicSensitiveLevel> { param ->
        val sensitiveLevel =
            call.receiveParameters()["sensitive_level"]?.toIntOrNull()?.let { SensitiveLevelSerializer.deserializeOrNull(it) } ?: return@patch call.respondApiError(HttpStatusCode.BadRequest) {
                "Essential \"sensitive_level\" is invalid."
            }

        if (Stella.PicCollection.countDocuments(PicEntry::_id eq ObjectId(param.id).toId()) == 0L) {
            return@patch call.respondApiError(HttpStatusCode.NotFound) {
                "Specified entry is not found."
            }
        }

        Stella.PicCollection.updateOne(
            PicEntry::_id eq ObjectId(param.id).toId(), combine(
                Updates.set(PicEntry::sensitive_level.name, SensitiveLevelSerializer.serialize(sensitiveLevel)),
                setValue(PicEntry::timestamp / PicEntry.Timestamp::manual_updated, Instant.now().toEpochMilli())
            )
        )
        val entry = Stella.PicCollection.findOne(PicEntry::_id eq ObjectId(param.id).toId())!!
        call.respond(entry)

        Stella.Logger.info {
            "${entry.url} のエントリが更新されました。sensitive_level が $sensitiveLevel に変更されました。(${call.request.origin.remoteHost})"
        }
    }
}
