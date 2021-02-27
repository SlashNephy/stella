package blue.starry.stella.models

import kotlinx.serialization.Serializable

@Serializable
data class PicSummaryModel(
    val entries: Long,
    val media: Long
)
