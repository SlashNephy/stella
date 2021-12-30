package blue.starry.stella.platforms.nijie.models

import kotlinx.serialization.Serializable

@Serializable
data class Picture(
    val title: String,
    val authorName: String,
    val authorUrl: String,
    val createdAt: Long,
    val media: List<String>,
    val description: String,
    val url: String,
    val id: String,
    val tags: List<String>,
    val like: Int,
    val bookmark: Int,
    val reply: Int,
    val view: Int
)
