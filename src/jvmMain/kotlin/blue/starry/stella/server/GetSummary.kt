package blue.starry.stella.server

import blue.starry.stella.Stella
import blue.starry.stella.models.PicSummary
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Files

fun Route.getSummary() {
    get("/summary") {
        call.respond(
            PicSummary(entries = Stella.PicCollection.countDocuments(), media = withContext(Dispatchers.IO) {
                @Suppress("BlockingMethodInNonBlockingContext") Files.list(Stella.MediaDirectory).count()
            })
        )
    }
}
