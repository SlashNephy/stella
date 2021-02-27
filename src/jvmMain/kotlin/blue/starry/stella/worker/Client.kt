package blue.starry.stella.worker

import blue.starry.penicillin.PenicillinClient
import blue.starry.penicillin.core.session.config.account
import blue.starry.penicillin.core.session.config.application
import blue.starry.penicillin.core.session.config.httpClient
import blue.starry.penicillin.core.session.config.token
import blue.starry.stella.Env
import blue.starry.stella.models.PicModel
import blue.starry.stella.models.PicTagReplaceTableModel
import blue.starry.stella.worker.platform.NijieClient
import blue.starry.stella.worker.platform.PixivClient
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.cookies.*
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

val StellaHttpClient by lazy {
    HttpClient(CIO) {
        install(HttpCookies)
    }
}

val StellaPixivClient by lazy {
    val (email, password) = Env.PIXIV_EMAIL to Env.PIXIV_PASSWORD
    if (email == null || password == null) {
        return@lazy null
    }

    PixivClient(email, password)
}

val StellaTwitterClient by lazy {
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
        httpClient(StellaHttpClient)
    }
}

val StellaNijieClient by lazy {
    val (email, password) = Env.NIJIE_EMAIL to Env.NIJIE_PASSWORD
    if (email == null || password == null) {
        return@lazy null
    }

    NijieClient(email, password)
}

val StellaMongoDBClient by lazy {
    val (user, password) = Env.DB_USER to Env.DB_PASSWORD

    if (user != null && password != null) {
        KMongo.createClient("mongodb://$user:$password@${Env.DB_HOST}:${Env.DB_PORT}")
    } else {
        KMongo.createClient("mongodb://${Env.DB_HOST}:${Env.DB_PORT}")
    }.coroutine
}

val StellaMongoDBDatabase by lazy {
    StellaMongoDBClient.getDatabase(Env.DB_NAME)
}

val StellaMongoDBPicCollection by lazy {
    StellaMongoDBDatabase.getCollection<PicModel>("Pic")
}

val StellaMongoDBPicTagReplaceTableCollection by lazy {
    StellaMongoDBDatabase.getCollection<PicTagReplaceTableModel>("PicTagReplaceTable")
}
