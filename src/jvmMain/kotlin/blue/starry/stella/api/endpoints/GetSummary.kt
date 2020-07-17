package blue.starry.stella.api.endpoints

import blue.starry.jsonkt.jsonObjectOf
import blue.starry.stella.api.respondApi
import blue.starry.stella.collection
import blue.starry.stella.mediaDirectory
import io.ktor.application.call
import io.ktor.routing.Route
import io.ktor.routing.get
import java.nio.file.Files

fun Route.getSummary() {
    get("/summary") {
        call.respondApi {
            jsonObjectOf(
                "entries" to collection.countDocuments(),
                "media" to Files.list(mediaDirectory).count()
            )
        }
    }
}
