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
import io.ktor.http.URLProtocol
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
import org.slf4j.event.Level
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
        static(Env.HTTP_BASE_URI) {
            staticRootFolder = File(Env.STATIC_DIRECTORY)

            files(".")
            default("index.html")
        }

        route(Env.HTTP_BASE_URI) {
            route("api") {
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
    }

    if (Env.BEHIND_REVERSE_PROXY) {
        install(XForwardedHeaderSupport)
    }

    if (Env.ENABLE_CORS) {
        install(CORS) {
            if (Env.CORS_HOSTS.isNotEmpty()) {
                val schemes = if (Env.CORS_ACCEPT_HTTP) {
                    listOf(URLProtocol.HTTP, URLProtocol.HTTPS)
                } else {
                    listOf(URLProtocol.HTTPS)
                }.map { it.name }

                for (host in Env.CORS_HOSTS) {
                    host(host, schemes = schemes)
                }
            } else {
                anyHost()
            }

            header("x-requested-with")
            for (header in Env.CORS_HEADERS) {
                header(header)
            }

            methods += setOf(
                HttpMethod.Get, HttpMethod.Post, HttpMethod.Patch, HttpMethod.Delete, HttpMethod.Put, HttpMethod.Options
            )

            allowNonSimpleContentTypes = true
        }
    }

    install(CallLogging) {
        logger = KotlinLogging.create("Stella.Http.Server")
        level = Level.DEBUG

        format { call ->
            when (val status = call.response.status()) {
                HttpStatusCode.Found -> "$status: ${call.request.toLogString()} -> ${call.response.headers[HttpHeaders.Location]}"
                null -> ""
                else -> "$status: ${call.request.httpMethod.value} ${call.request.uri}"
            }
        }
    }
}
