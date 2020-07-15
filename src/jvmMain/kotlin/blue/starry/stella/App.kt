package blue.starry.stella

import blue.starry.stella.api.getQuery
import blue.starry.stella.api.getSummary
import io.ktor.application.Application
import io.ktor.http.content.resource
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.util.KtorExperimentalAPI
import org.bson.Document
import org.litote.kmongo.coroutine.CoroutineClient
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import java.nio.file.Files
import java.nio.file.Paths

internal val mediaDirectory = Paths.get("media")
internal lateinit var mongodb: CoroutineClient
internal lateinit var collection: CoroutineCollection<Document>

@KtorExperimentalAPI
fun Application.main() {
    if (!Files.exists(mediaDirectory)) {
        Files.createDirectory(mediaDirectory)
    }

    val mongodbHost = environment.config.propertyOrNull("database.mongodb.host")?.getString() ?: "127.0.0.1"
    val mongodbPort = environment.config.propertyOrNull("database.mongodb.port")?.getString() ?: "27017"
    mongodb = KMongo.createClient("mongodb://$mongodbHost:$mongodbPort").coroutine

    var mongodbDatabase = environment.config.propertyOrNull("database.mongodb.database")?.getString() ?: "bot"
    var mongodbCollection = environment.config.propertyOrNull("database.mongodb.collection")?.getString() ?: "Pic"
    collection = mongodb.getDatabase(mongodbDatabase).getCollection(mongodbCollection)

    routing {
        resource("/", "index.html")

        route("/api") {
            getQuery()
            getSummary()
        }

        static("/static") {
            resources("static")
            resource("stella.js")
        }
    }
}
