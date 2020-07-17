package blue.starry.stella.api.endpoints

import blue.starry.jsonkt.jsonObjectOf
import blue.starry.stella.api.respondApi
import blue.starry.stella.api.toPic
import blue.starry.stella.collection
import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Filters
import io.ktor.application.call
import io.ktor.routing.Route
import io.ktor.routing.get
import org.bson.Document
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import org.litote.kmongo.coroutine.aggregate

fun Route.getQueryTags() {
    get("/query/tags") {
        val count = call.parameters["count"]?.toIntOrNull() ?: 10
        val random = call.parameters["random"]?.toBoolean() ?: false

        val id = call.parameters["id"]
        val existTags = id?.let {
            collection.findOne(Filters.eq("_id", ObjectId(it)))?.toPic()?.tags
        }.orEmpty().map {
            it.value
        }

        val filter = mutableListOf<Bson>().also { filters ->
            filters += Filters.not(Filters.size("tags", 0))

            val name = call.parameters["name"]
            if (name != null) {
                filters += Filters.elemMatch("tags", Filters.regex("value", name, "i"))
            }

            if (existTags.isNotEmpty()) {
                filters += Filters.or(existTags.map {
                    Filters.elemMatch("tags", Filters.regex("value", it, "i"))
                })
            }

            val sensitiveLevel = call.parameters["sensitive_level"]?.toIntOrNull()
            if (sensitiveLevel != null) {
                filters += Filters.lte("sensitive_level", sensitiveLevel)
            }
        }

        val tags = collection.aggregate<Document>(Aggregates.match(Filters.and(filter))).toList().flatMap {
            it.toPic().tags
        }.map {
            it.value
        }.distinct().let {
            it - existTags
        }.let {
            if (random) {
                it.shuffled()
            } else {
                it
            }
        }.take(count)

        call.respondApi {
            jsonObjectOf(
                "tags" to tags
            )
        }
    }
}
