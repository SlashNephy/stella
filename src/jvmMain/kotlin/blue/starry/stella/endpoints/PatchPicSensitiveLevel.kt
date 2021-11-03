package blue.starry.stella.endpoints

import blue.starry.stella.logger
import blue.starry.stella.models.PicModel
import blue.starry.stella.models.internal.SensitiveLevelSerializer
import blue.starry.stella.worker.StellaMongoDBPicCollection
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
        val sensitiveLevel = call.receiveParameters()["sensitive_level"]
            ?.toIntOrNull()
            ?.let { SensitiveLevelSerializer.deserializeOrNull(it) }
            ?: return@patch call.respondApiError(HttpStatusCode.BadRequest) {
                "Essential \"sensitive_level\" is invalid."
            }

        if (StellaMongoDBPicCollection.countDocuments(PicModel::_id eq ObjectId(param.id).toId()) == 0L) {
            return@patch call.respondApiError(HttpStatusCode.NotFound) {
                "Specified entry is not found."
            }
        }

        StellaMongoDBPicCollection.updateOne(
            PicModel::_id eq ObjectId(param.id).toId(),
            combine(
                Updates.set(PicModel::sensitive_level.name, SensitiveLevelSerializer.serialize(sensitiveLevel)),
                setValue(PicModel::timestamp / PicModel.Timestamp::manual_updated, Instant.now().toEpochMilli())
            )
        )

        val entry = StellaMongoDBPicCollection.findOne(PicModel::_id eq ObjectId(param.id).toId())!!
        call.respond(entry)

        logger.info {
            "${entry.url} のエントリが更新されました。sensitive_level が $sensitiveLevel に変更されました。(${call.request.origin.remoteHost})"
        }
    }
}
