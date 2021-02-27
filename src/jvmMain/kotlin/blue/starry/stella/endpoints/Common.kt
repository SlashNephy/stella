package blue.starry.stella.endpoints

import blue.starry.stella.models.ApiErrorModel
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*

suspend fun ApplicationCall.respondApiError(
    code: HttpStatusCode = HttpStatusCode.InternalServerError,
    block: () -> String
) {
    respond(
        ApiErrorModel(
            code = code.value,
            message = block()
        )
    )
}
