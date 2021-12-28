package blue.starry.stella.register

import blue.starry.stella.models.PicEntry

data class PicRegistration(
    val title: String,
    val description: String,
    val url: String,
    val tags: List<String>,
    val platform: PicEntry.Platform,
    val sensitiveLevel: PicEntry.SensitiveLevel,
    val created: Long,
    val author: Author,
    val media: List<Picture>,
    val popularity: Popularity
) {
    data class Author(
        val name: String,
        val url: String,
        val username: String?
    )

    data class Picture(
        val index: Int,
        val filename: String,
        val original: String,
        val ext: String
    )

    data class Popularity(
        val like: Int? = null,
        val bookmark: Int? = null,
        val view: Int? = null,
        val retweet: Int? = null,
        val reply: Int? = null
    )
}
