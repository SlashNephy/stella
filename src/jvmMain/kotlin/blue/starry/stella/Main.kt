package blue.starry.stella

import blue.starry.stella.server.entrypoint
import blue.starry.stella.worker.*
import io.ktor.application.Application
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import kotlin.io.path.createDirectories
import kotlin.io.path.notExists

fun main() {
    if (Stella.MediaDirectory.notExists()) {
        Stella.MediaDirectory.createDirectories()
    }

    val workers = listOf(
        DatabaseMigrationWorker(),
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
