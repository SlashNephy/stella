package blue.starry.stella.api

import blue.starry.jsonkt.*
import io.ktor.application.ApplicationCall
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.response.respondText
import org.bson.Document
import org.bson.json.JsonWriterSettings

private val pureJsonWriter = JsonWriterSettings.builder().int64Converter { value, writer ->
    writer.writeRaw(value.toString())
}.build()

fun List<Document>.serialize(): JsonArray {
    return map {
        it.toJson(pureJsonWriter).toJsonObject().copy { map ->
            map["id"] = (map.remove("_id") as JsonObject)["\$oid"]
        }
    }.toJsonArray()
}

suspend fun ApplicationCall.respondApi(block: suspend () -> JsonElement) {
    runCatching {
        block()
    }.onSuccess {
        respondText(contentType = ContentType.Application.Json) {
            jsonObjectOf(
                "success" to true,
                "result" to it,
                "error" to null
            ).stringify()
        }
    }.onFailure {
        respondText(contentType = ContentType.Application.Json) {
            jsonObjectOf(
                "success" to false,
                "result" to null,
                "error" to mapOf(
                    "code" to HttpStatusCode.InternalServerError.value,
                    "message" to it.message
                )
            ).stringify()
        }
    }
}
