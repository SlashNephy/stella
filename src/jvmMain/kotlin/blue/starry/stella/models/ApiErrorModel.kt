package blue.starry.stella.models

import kotlinx.serialization.Serializable

@Serializable
data class ApiErrorModel(
    val code: Int,
    val message: String
)
