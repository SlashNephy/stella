package blue.starry.stella.models

import kotlinx.serialization.Serializable

@Serializable
data class PicTagsModel(
    val tags: List<String>
)
