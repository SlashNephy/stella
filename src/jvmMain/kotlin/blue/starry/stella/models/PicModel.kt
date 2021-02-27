package blue.starry.stella.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.litote.kmongo.Id

@Serializable
data class PicModel(
    @SerialName("_id") @Contextual val id: Id<PicModel>,
    val title: String,
    val description: String,
    val url: String,
    val tags: List<Tag>,
    val user: String?,

    val platform: String,
    val sensitive_level: Int,

    val timestamp: Timestamp,
    val author: Author,
    val media: List<Media>,

    val rating: Rating,
    val popularity: Popularity
) {
    @Serializable
    data class Tag(
        val value: String,
        val user: String?,
        val locked: Boolean
    )

    @Serializable
    data class Timestamp(
        val created: Long,
        val added: Long,
        val manual_updated: Long,
        val auto_updated: Long
    )

    @Serializable
    data class Author(
        val name: String,
        val username: String?,
        val url: String
    )

    @Serializable
    data class Media(
        val index: Int,
        val filename: String,
        val original: String,
        val ext: String
    )

    @Serializable
    data class Rating(
        val count: Int,
        val score: Int
    )

    @Serializable
    data class Popularity(
        val like: Int?,
        val bookmark: Int?,
        val view: Int?,
        val retweet: Int?,
        val reply: Int?
    )
}
