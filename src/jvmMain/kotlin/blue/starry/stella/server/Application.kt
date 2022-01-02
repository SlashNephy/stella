package blue.starry.stella.server

import blue.starry.stella.Env
import blue.starry.stella.create
import blue.starry.stella.server.endpoints.*
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.*
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.default
import io.ktor.http.content.files
import io.ktor.http.content.static
import io.ktor.http.content.staticRootFolder
import io.ktor.locations.Locations
import io.ktor.request.httpMethod
import io.ktor.request.uri
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.serialization.json
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import org.litote.kmongo.id.serialization.IdKotlinXSerializationModule
import java.io.File

fun Application.entrypoint() {
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
        logger = KotlinLogging.create("Stella.Server")
        format { call ->
            when (val status = call.response.status()) {
                HttpStatusCode.Found -> "$status: ${call.request.toLogString()} -> ${call.response.headers[HttpHeaders.Location]}"
                null -> ""
                else -> "$status: ${call.request.httpMethod.value} ${call.request.uri}"
            }
        }
    }
}
