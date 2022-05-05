package blue.starry.stella.server.endpoints

import blue.starry.stella.Stella
import blue.starry.stella.models.PicSummary
import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Files

fun Route.getSummary() {
    get("/summary") {
        call.respond(
            PicSummary(
                entries = Stella.PicCollection.countDocuments(),
                media = withContext(Dispatchers.IO) {
                    Files.list(Stella.MediaDirectory).count()
                }
            )
        )
    }
}
