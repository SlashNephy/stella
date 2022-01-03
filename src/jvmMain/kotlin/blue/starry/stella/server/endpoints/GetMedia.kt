package blue.starry.stella.server.endpoints

import blue.starry.stella.Env
import blue.starry.stella.Stella
import io.ktor.application.call
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.request.header
import io.ktor.response.respond
import io.ktor.response.respondFile
import io.ktor.routing.Route

@Location("/media/{filename}")
data class GetMedia(val filename: String)

fun Route.getMediaByFilename() {
    get<GetMedia> { (filename) ->
        if (Env.CORS_HOSTS.isNotEmpty()) {
            val referer = call.request.header(HttpHeaders.Referrer) ?: return@get call.respond(HttpStatusCode.BadRequest)
            val refererUrl = Url(referer)
            if (refererUrl.host !in Env.CORS_HOSTS) {
                return@get call.respond(HttpStatusCode.Forbidden)
            }
        }

        call.respondFile(Stella.MediaDirectory.toFile(), filename)
    }
}
