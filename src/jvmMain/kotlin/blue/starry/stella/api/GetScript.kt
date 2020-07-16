package blue.starry.stella.api

import io.ktor.application.call
import io.ktor.response.respondFile
import io.ktor.routing.Route
import io.ktor.routing.get
import java.nio.file.Paths

fun Route.getScript() {
    get("/script") {
        val script = Paths.get("build/distributions/stella.js")
        call.respondFile(script.toFile())
    }
}
