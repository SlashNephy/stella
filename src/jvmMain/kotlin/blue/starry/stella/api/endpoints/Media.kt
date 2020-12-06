package blue.starry.stella.api.endpoints

import blue.starry.stella.mediaDirectory
import io.ktor.application.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*

@Location("/media/{filename}")
data class Media(val filename: String)

fun Route.getMedia() {
    get<Media> { (filename) ->
        call.respondFile(mediaDirectory.toFile(), filename)
    }
}
