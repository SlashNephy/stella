package blue.starry.stella.api

import blue.starry.jsonkt.*
import blue.starry.stella.common.PicModel
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import org.bson.Document
import org.bson.json.JsonWriterSettings

private val pureJsonWriter = JsonWriterSettings.builder().int64Converter { value, writer ->
    writer.writeRaw(value.toString())
}.build()

fun Document.serialize(): JsonObject {
    return toJson(pureJsonWriter).toJsonObject().copy { map ->
        map["id"] = (map.remove("_id") as JsonObject)["\$oid"]
    }
}

fun List<Document>.serialize(): JsonArray {
    return map {
        it.serialize()
    }.toJsonArray()
}

fun Document.toPic(): PicModel {
    return serialize().parseObject { PicModel(it) }
}

fun Document.toTagReplaceTable(): TagReplaceTableModel {
    return serialize().parseObject { TagReplaceTableModel(it) }
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
            ).encodeToString()
        }
    }.onFailure {
        respondApiError {
            it.message ?: "Internal server error."
        }
    }
}

suspend fun ApplicationCall.respondApiError(
    code: HttpStatusCode = HttpStatusCode.InternalServerError,
    block: () -> String
) {
    respondText(contentType = ContentType.Application.Json) {
        jsonObjectOf(
            "success" to false,
            "result" to null,
            "error" to mapOf(
                "code" to code.value,
                "message" to block()
            )
        ).encodeToString()
    }
}
