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
import io.ktor.locations.patch
import io.ktor.request.receiveParameters
import io.ktor.routing.Route
import org.bson.types.ObjectId
import java.util.*

@KtorExperimentalLocationsAPI
@Location("/edit/{id}/sensitive_level")
data class EditSensitiveLevel(val id: String)

@KtorExperimentalLocationsAPI
fun Route.patchSensitiveLevel() {
    patch<EditSensitiveLevel> { (id) ->
        val level = call.receiveParameters()["sensitive_level"]?.toIntOrNull()
        if (level !in 0..3) {
            call.respondApiError(HttpStatusCode.BadRequest) {
                "Essential \"sensitive_level\" is invalid or not present."
            }

            return@patch
        }

        if (collection.countDocuments(Filters.eq("_id", ObjectId(id))) == 0L) {
            call.respondApiError(HttpStatusCode.NotFound) {
                "Specified entry is not found."
            }

            return@patch
        }

        collection.updateOne(
            Filters.eq("_id", ObjectId(id)),
            Updates.combine(
                Updates.set("sensitive_level", level),
                Updates.set("timestamp.manual_updated", Calendar.getInstance().timeInMillis)
            )
        )

        val entry = collection.findOne(Filters.eq("_id", ObjectId(id)))!!
        call.respondApi {
            entry.serialize()
        }

        logger.info {
            val pic = entry.toPic()
            "${pic.url} のエントリが更新されました。sensitive_level が $level に変更されました。(${call.request.origin.remoteHost})"
        }
    }
}
