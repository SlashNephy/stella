package blue.starry.stella.platforms.pixiv.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UgoiraMetadata(
    val frames: List<Frame>,
    @SerialName("zip_urls")
    val zipUrls: ZipUrls
) {
    @Serializable
    data class Frame(
        val delay: Int,
        val `file`: String
    )

    @Serializable
    data class ZipUrls(
        val medium: String
    )
}
