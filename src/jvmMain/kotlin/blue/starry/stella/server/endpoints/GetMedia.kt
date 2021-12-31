package blue.starry.stella.server.endpoints

import blue.starry.stella.Stella
import io.ktor.application.call
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respondFile
import io.ktor.routing.Route

@Location("/media/{filename}")
data class GetMedia(val filename: String)

fun Route.getMediaByFilename() {
    get<GetMedia> { (filename) ->
        call.respondFile(Stella.MediaDirectory.toFile(), filename)
    }
}
