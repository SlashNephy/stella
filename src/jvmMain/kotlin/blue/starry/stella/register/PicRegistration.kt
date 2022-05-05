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
    val author: PicEntry.Author,
    val media: List<PicEntry.Media>,
    val popularity: PicEntry.Popularity
)
