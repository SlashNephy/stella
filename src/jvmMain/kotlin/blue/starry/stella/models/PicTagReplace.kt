package blue.starry.stella.models

import kotlinx.serialization.Serializable

@Serializable
data class PicTagReplace(
    val from: String,
    val to: String
)
