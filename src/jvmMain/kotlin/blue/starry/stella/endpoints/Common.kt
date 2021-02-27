package blue.starry.stella.endpoints

import blue.starry.stella.models.ApiResponseModel
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*

suspend fun ApplicationCall.respondApiResponse(result: Any) {
    respond(
        ApiResponseModel(
            success = true,
            result = result,
            error = null
        )
    )
}

suspend fun ApplicationCall.respondApiError(
    code: HttpStatusCode = HttpStatusCode.InternalServerError,
    block: () -> String
) {
    respond(
        ApiResponseModel(
            success = true,
            result = null,
            error = ApiResponseModel.Error(
                code = code.value,
                message = block()
            )
        )
    )
}
