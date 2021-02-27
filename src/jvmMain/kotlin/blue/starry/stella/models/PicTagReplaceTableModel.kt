package blue.starry.stella.models

import kotlinx.serialization.Serializable

@Serializable
data class PicTagReplaceTableModel(
    val from: String,
    val to: String
)
