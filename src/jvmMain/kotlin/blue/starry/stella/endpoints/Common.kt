package blue.starry.stella.endpoints

import blue.starry.stella.models.ApiError
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*

suspend fun ApplicationCall.respondApiError(
    code: HttpStatusCode = HttpStatusCode.InternalServerError,
    block: () -> String
) {
    respond(
        ApiError(
            code = code.value,
            message = block()
        )
    )
}
