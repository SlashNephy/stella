package blue.starry.stella

import blue.starry.penicillin.PenicillinClient
import blue.starry.penicillin.core.session.ApiClient
import blue.starry.penicillin.core.session.config.account
import blue.starry.penicillin.core.session.config.application
import blue.starry.penicillin.core.session.config.httpClient
import blue.starry.penicillin.core.session.config.token
import blue.starry.stella.models.PicEntry
import blue.starry.stella.models.PicTagReplace
import blue.starry.stella.platforms.nijie.NijieClient
import blue.starry.stella.platforms.pixiv.PixivClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.cookies.AcceptAllCookiesStorage
import io.ktor.client.features.cookies.HttpCookies
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logger
import io.ktor.client.features.logging.Logging
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import mu.KLogger
import mu.KotlinLogging
import org.litote.kmongo.coroutine.CoroutineClient
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import java.nio.file.Path
import java.nio.file.Paths

@OptIn(ObsoleteCoroutinesApi::class, ExperimentalSerializationApi::class)
object Stella {
    val Logger: KLogger = KotlinLogging.logger("Stella")
    val MediaDirectory: Path = Paths.get("media")

    val Dispatcher: CoroutineDispatcher = newFixedThreadPoolContext(
        Runtime.getRuntime().availableProcessors() * 2,
        "StellaDispatcher"
    )

    val Http: HttpClient = HttpClient(CIO) {
        install(HttpCookies) {
            storage = AcceptAllCookiesStorage()
        }

        install(JsonFeature) {
            serializer = KotlinxSerializer(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
                explicitNulls = false
            })
        }

        Logging {
            level = LogLevel.INFO
            logger = object : Logger {
                private val logger = KotlinLogging.logger("Stella.Http")

                override fun log(message: String) {
                    logger.trace { message }
                }
            }
        }
    }

    val Pixiv: PixivClient? by lazy {
        val token = Env.PIXIV_REFRESH_TOKEN ?: return@lazy null

        PixivClient(token)
    }

    val Twitter: ApiClient? by lazy {
        val (ck, cs) = Env.TWITTER_CK to Env.TWITTER_CS
        val (at, ats) = Env.TWITTER_AT to Env.TWITTER_ATS
        if(ck == null || cs == null || at == null || ats == null) {
            return@lazy null
        }

        PenicillinClient {
            account {
                application(ck, cs)
                token(at, ats)
            }
            httpClient(Http)
        }
    }

    val Nijie: NijieClient? by lazy {
        val (email, password) = Env.NIJIE_EMAIL to Env.NIJIE_PASSWORD
        if (email == null || password == null) {
            return@lazy null
        }

        NijieClient(email, password)
    }

    private val Mongo: CoroutineClient by lazy {
        val (user, password) = Env.DB_USER to Env.DB_PASSWORD

        if (user != null && password != null) {
            KMongo.createClient("mongodb://$user:$password@${Env.DB_HOST}:${Env.DB_PORT}")
        } else {
            KMongo.createClient("mongodb://${Env.DB_HOST}:${Env.DB_PORT}")
        }.coroutine
    }

    private val Database: CoroutineDatabase by lazy {
        Mongo.getDatabase(Env.DB_NAME)
    }

    val PicCollection: CoroutineCollection<PicEntry> by lazy {
        Database.getCollection("Pic")
    }

    val TagReplaceCollection: CoroutineCollection<PicTagReplace> by lazy {
        Database.getCollection("PicTagReplaceTable")
    }
}
