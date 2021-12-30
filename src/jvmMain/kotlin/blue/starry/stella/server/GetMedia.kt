package blue.starry.stella.server

import blue.starry.stella.Stella
import io.ktor.application.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*

@Location("/media/{filename}")
data class GetMedia(val filename: String)

fun Route.getMediaByFilename() {
    get<GetMedia> { (filename) ->
        call.respondFile(Stella.MediaDirectory.toFile(), filename)
    }
}
