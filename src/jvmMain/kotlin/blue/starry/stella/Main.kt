package blue.starry.stella

import blue.starry.stella.platforms.pixiv.PixivSourceProvider
import blue.starry.stella.server.entrypoint
import blue.starry.stella.worker.*
import io.ktor.application.Application
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import kotlinx.coroutines.runBlocking
import java.nio.file.Files

fun main() {
    if (!Files.exists(Stella.MediaDirectory)) {
        Files.createDirectory(Stella.MediaDirectory)
    }

    runBlocking {
        PixivSourceProvider.registerById(94850870, false)
    }

    val workers = listOf(
        RefreshEntryWorker(),
        RefetchMissingMediaWorker(),
        WatchTwitterWorker(),
        WatchPixivWorker(),
        WatchNijieWorker()
    )

    for (worker in workers) {
        worker.start()
    }

    embeddedServer(
        factory = CIO,
        host = Env.HTTP_HOST,
        port = Env.HTTP_PORT,
        module = Application::entrypoint
    ).start(wait = true)
}
