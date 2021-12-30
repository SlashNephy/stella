package blue.starry.stella.platforms.pixiv.models

import blue.starry.stella.platforms.pixiv.entities.Illust
import kotlinx.serialization.Serializable

@Serializable
data class BookmarksResponse(val illusts: List<Illust>, val nextUrl: String?)
