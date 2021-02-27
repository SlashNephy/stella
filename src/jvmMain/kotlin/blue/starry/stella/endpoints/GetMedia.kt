package blue.starry.stella.endpoints

import blue.starry.stella.mediaDirectory
import io.ktor.application.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*

@Location("/media/{filename}")
data class GetMedia(val filename: String)

fun Route.getMedia() {
    get<GetMedia> { (filename) ->
        call.respondFile(mediaDirectory.toFile(), filename)
    }
}
