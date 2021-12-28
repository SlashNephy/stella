package blue.starry.stella.models

import blue.starry.stella.models.internal.PlatformSerializer
import blue.starry.stella.models.internal.SensitiveLevelSerializer
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.litote.kmongo.Id

@Serializable
data class PicEntry(
    @Contextual val _id: Id<PicEntry>,
    val title: String,
    val description: String,
    val url: String,
    val tags: List<Tag>,
    val user: String?,

    val platform: Platform,
    val sensitive_level: SensitiveLevel,

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
        val manual_updated: Long?,
        val auto_updated: Long?
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

    @Serializable(PlatformSerializer::class)
    @Suppress("unused")
    enum class Platform {
        Twitter,
        Pixiv,
        Nijie
    }

    @Serializable(SensitiveLevelSerializer::class)
    @Suppress("unused")
    enum class SensitiveLevel {
        Safe,
        R15,
        R18,
        R18G
    }
}
