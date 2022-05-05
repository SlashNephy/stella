package blue.starry.stella.platforms.pixiv.models

import blue.starry.stella.platforms.pixiv.entities.UgoiraMetadata
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UgoiraMetadataResponse(
    @SerialName("ugoira_metadata")
    val ugoiraMetadata: UgoiraMetadata
)
