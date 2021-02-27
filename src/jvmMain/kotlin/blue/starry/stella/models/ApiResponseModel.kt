package blue.starry.stella.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class ApiResponseModel(
    val success: Boolean,
    @Contextual val result: Any?,
    val error: Error?
) {
    @Serializable
    data class Error(
        val code: Int,
        val message: String
    )
}
