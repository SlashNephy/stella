package blue.starry.stella.endpoints

import blue.starry.stella.logger
import blue.starry.stella.models.PicModel
import blue.starry.stella.worker.StellaMongoDBPicCollection
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.locations.patch
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.Route
import org.litote.kmongo.*
import java.time.Instant
import java.util.*

@Location("/pic/{id}/sensitive_level")
data class PatchPicSensitiveLevel(val id: String, val sensitive_level: Int)

fun Route.patchPicSensitiveLevel() {
    patch<PatchPicSensitiveLevel> { param ->
        if (param.sensitive_level !in 0..3) {
            return@patch call.respondApiError(HttpStatusCode.BadRequest) {
                "Essential \"sensitive_level\" is invalid."
            }
        }

        if (StellaMongoDBPicCollection.countDocuments(PicModel::_id eq param.id.toId()) == 0L) {
            return@patch call.respondApiError(HttpStatusCode.NotFound) {
                "Specified entry is not found."
            }
        }

        StellaMongoDBPicCollection.updateOne(
            PicModel::_id eq param.id.toId(),
            combine(
                setValue(PicModel::sensitive_level, param.sensitive_level),
                setValue(PicModel::timestamp / PicModel.Timestamp::manual_updated, Instant.now().toEpochMilli())
            )
        )

        val entry = StellaMongoDBPicCollection.findOne(PicModel::_id eq param.id.toId())!!
        call.respond(entry)

        logger.info {
            "${entry.url} のエントリが更新されました。sensitive_level が ${param.sensitive_level} に変更されました。(${call.request.origin.remoteHost})"
        }
    }
}
