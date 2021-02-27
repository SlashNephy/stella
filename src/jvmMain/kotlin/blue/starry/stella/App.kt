package blue.starry.stella

import blue.starry.stella.endpoints.*
import blue.starry.stella.worker.MissingMediaRefetchWorker
import blue.starry.stella.worker.RefreshWorker
import blue.starry.stella.worker.platform.NijieSourceProvider
import blue.starry.stella.worker.platform.PixivSourceProvider
import blue.starry.stella.worker.platform.TwitterSourceProvider
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import org.litote.kmongo.id.serialization.IdKotlinXSerializationModule
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

internal val logger = KotlinLogging.logger("Stella")
internal val mediaDirectory = Paths.get("media")

fun main() {
    if (!Files.exists(mediaDirectory)) {
        Files.createDirectory(mediaDirectory)
    }

    RefreshWorker.start()
    MissingMediaRefetchWorker.start()

    TwitterSourceProvider.start()
    PixivSourceProvider.start()
    NijieSourceProvider.start()

    embeddedServer(CIO, host = Env.HTTP_HOST, port = Env.HTTP_PORT, module = Application::module).start(wait = true)
}

fun Application.module() {
    install(Locations)

    install(ContentNegotiation) {
        json(Json {
            encodeDefaults = true
            serializersModule = IdKotlinXSerializationModule
        })
    }

    routing {
        static("/") {
            staticRootFolder = File("docs")

            static("static") {
                files("static")
            }
            default("index.html")
        }

        route("/api") {
            getQuery()
            getQueryTags()
            getSummary()
            getMediaByFilename()
            putPicRefresh()
            putPicTag()
            deletePicTag()
            patchPicSensitiveLevel()
        }
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

        val host = Env.HOST ?: return@install
        host(host, schemes = listOf("http", "https"))
    }

    install(CallLogging) {
        logger = KotlinLogging.logger("stella.web")
        format { call ->
            when (val status = call.response.status()) {
                HttpStatusCode.Found -> "$status: ${call.request.toLogString()} -> ${call.response.headers[HttpHeaders.Location]}"
                null -> ""
                else -> "$status: ${call.request.httpMethod.value} ${call.request.uri}"
            }
        }
    }
}
