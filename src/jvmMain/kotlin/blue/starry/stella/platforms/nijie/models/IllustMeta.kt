package blue.starry.stella.platforms.nijie.models

import blue.starry.stella.platforms.nijie.entities.Illust

data class IllustMeta(
    val illust: Illust,
    val mediaUrls: List<String>,
    val url: String,
    val id: String,
    val tags: List<String>,
    val like: Int,
    val bookmark: Int,
    val reply: Int,
    val view: Int,
    val isFollowing: Boolean,
    val userId: String?
)
