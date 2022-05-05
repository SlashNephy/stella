package blue.starry.stella.platforms.nijie.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ViewCount(
    @SerialName("view_count")
    val viewCount: Int
)
