package blue.starry.stella

import blue.starry.stella.api.endpoints.*
import blue.starry.stella.worker.MissingMediaRefetchWorker
import blue.starry.stella.worker.RefreshWorker
import blue.starry.stella.worker.platform.NijieSourceProvider
import blue.starry.stella.worker.platform.PixivSourceProvider
import blue.starry.stella.worker.platform.TwitterSourceProvider
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import mu.KotlinLogging
import java.nio.file.Files
import java.nio.file.Paths

internal val logger = KotlinLogging.logger("Stella")
internal val mediaDirectory = Paths.get("media")

fun main() {
    embeddedServer(Netty, host = Config.HttpHost, port = Config.HttpPort, module = Application::module).start(wait = true)
}

fun Application.module() {
    if (!Files.exists(mediaDirectory)) {
        Files.createDirectory(mediaDirectory)
    }

    install(Locations)

    routing {
        getMedia()
        getScript()
        getQuery()
        getSummary()
        putRefresh()
        getQueryTags()
        putEditTag()
        deleteEditTag()
        patchSensitiveLevel()
    }

    install(XForwardedHeaderSupport)

    install(CORS) {
        method(HttpMethod.Options)
        method(HttpMethod.Get)
        method(HttpMethod.Post)
        method(HttpMethod.Put)
        method(HttpMethod.Delete)
        method(HttpMethod.Patch)
        allowNonSimpleContentTypes = true
        header("x-requested-with")

        if (Config.Host != null) {
            host(Config.Host, schemes = listOf("http", "https"))
        }
    }

    RefreshWorker.start()
    MissingMediaRefetchWorker.start()

    if (Config.TwitterConsumerKey != null && Config.TwitterConsumerSecret != null && Config.TwitterAccessToken != null && Config.TwitterAccessTokenSecret != null) {
        TwitterSourceProvider.start()
    }

    if (Config.PixivEmail != null && Config.PixivPassword != null) {
        PixivSourceProvider.start()
    }

    if (Config.NijieEmail != null && Config.NijiePassword != null) {
        NijieSourceProvider.start(Config.NijieEmail, Config.NijiePassword)
    }
}
