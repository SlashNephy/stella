package blue.starry.stella.models

import kotlinx.serialization.Serializable

@Serializable
data class PicTags(
    val tags: List<String>
)
