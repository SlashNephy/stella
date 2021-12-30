package blue.starry.stella.platforms.pixiv.models

import blue.starry.stella.platforms.pixiv.entities.Illust

data class BookmarksResponse(val illusts: List<Illust>, val nextUrl: String?)
