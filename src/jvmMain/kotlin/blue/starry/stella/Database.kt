package blue.starry.stella

import org.bson.Document
import org.litote.kmongo.coroutine.CoroutineClient
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

internal val mongodb: CoroutineClient by lazy {
    if (Config.DatabaseUser != null && Config.DatabasePassword != null) {
        KMongo.createClient("mongodb://${Config.DatabaseUser}:${Config.DatabasePassword}@${Config.DatabaseHost}:${Config.DatabasePort}")
    } else {
        KMongo.createClient("mongodb://${Config.DatabaseHost}:${Config.DatabasePort}")
    }.coroutine
}

internal val database: CoroutineDatabase by lazy {
    mongodb.getDatabase(Config.DatabaseName)
}

internal val collection: CoroutineCollection<Document> by lazy {
    database.getCollection("Pic")
}

internal val tagReplaceTable: CoroutineCollection<Document> by lazy {
    database.getCollection("PicTagReplaceTable")
}
