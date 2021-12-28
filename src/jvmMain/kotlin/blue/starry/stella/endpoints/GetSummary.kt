package blue.starry.stella.endpoints

import blue.starry.stella.mediaDirectory
import blue.starry.stella.models.PicSummary
import blue.starry.stella.worker.StellaMongoDBPicCollection
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Files

fun Route.getSummary() {
    get("/summary") {
        call.respond(
            PicSummary(
                entries = StellaMongoDBPicCollection.countDocuments(),
                media = withContext(Dispatchers.IO) {
                    @Suppress("BlockingMethodInNonBlockingContext")
                    Files.list(mediaDirectory).count()
                }
            )
        )
    }
}
