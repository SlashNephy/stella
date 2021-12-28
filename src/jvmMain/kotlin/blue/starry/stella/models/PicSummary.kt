package blue.starry.stella.models

import kotlinx.serialization.Serializable

@Serializable
data class PicSummary(
    val entries: Long,
    val media: Long
)
