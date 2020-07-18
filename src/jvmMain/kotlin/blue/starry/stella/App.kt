package blue.starry.stella

import blue.starry.stella.api.endpoints.*
import blue.starry.stella.worker.MissingMediaRefetchWorker
import blue.starry.stella.worker.RefreshWorker
import blue.starry.stella.worker.platform.NijieSourceProvider
import blue.starry.stella.worker.platform.PixivSourceProvider
import blue.starry.stella.worker.platform.TwitterSourceProvider
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.config.ApplicationConfig
import io.ktor.features.CORS
import io.ktor.features.XForwardedHeaderSupport
import io.ktor.http.HttpMethod
import io.ktor.locations.Locations
import io.ktor.routing.routing
import mu.KotlinLogging
import org.bson.Document
import org.litote.kmongo.coroutine.CoroutineClient
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import java.nio.file.Files
import java.nio.file.Paths

internal val logger = KotlinLogging.logger("Stella")
internal val mediaDirectory = Paths.get("media")
internal lateinit var config: ApplicationConfig

internal val mongodb: CoroutineClient by lazy {
    val mongodbHost = config.propertyOrNull("database.mongodb.host")?.getString() ?: "127.0.0.1"
    val mongodbPort = config.propertyOrNull("database.mongodb.port")?.getString() ?: "27017"

    KMongo.createClient("mongodb://$mongodbHost:$mongodbPort").coroutine
}
internal val database: CoroutineDatabase by lazy {
    val mongodbDatabase = config.propertyOrNull("database.mongodb.database")?.getString() ?: "bot"

    mongodb.getDatabase(mongodbDatabase)
}
internal val collection: CoroutineCollection<Document> by lazy {
    database.getCollection<Document>("Pic")
}
internal val tagReplaceTable: CoroutineCollection<Document> by lazy {
    database.getCollection<Document>("PicTagReplaceTable")
}

@Suppress("Unused")
fun Application.main() {
    config = environment.config

    if (!Files.exists(mediaDirectory)) {
        Files.createDirectory(mediaDirectory)
    }

    install(Locations)

    routing {
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

        host("stella.starry.blue", schemes = listOf("https"))
    }

    RefreshWorker.start()
    MissingMediaRefetchWorker.start()

    TwitterSourceProvider.start()
    PixivSourceProvider.start()
    NijieSourceProvider.start()
}
