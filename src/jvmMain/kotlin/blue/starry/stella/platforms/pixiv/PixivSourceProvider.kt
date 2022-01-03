package blue.starry.stella.platforms.pixiv

import blue.starry.stella.Env
import blue.starry.stella.Stella
import blue.starry.stella.models.PicEntry
import blue.starry.stella.platforms.SourceProvider
import blue.starry.stella.platforms.pixiv.entities.Illust
import blue.starry.stella.register.MediaRegistory
import blue.starry.stella.register.PicRegistration
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object PixivSourceProvider: SourceProvider<Int, Illust> {
    private val UrlPattern = "^(?:https?://)?(?:www\\.)?pixiv\\.net/artworks/(?<id>\\d+)".toRegex()

    override suspend fun registerByUrl(url: String, auto: Boolean): Boolean {
        val match = UrlPattern.find(url) ?: return false
        val id = match.groups["id"]!!.value.toInt()

        return registerById(id, auto)
    }

    override suspend fun registerById(id: Int, auto: Boolean): Boolean {
        val client = Stella.Pixiv ?: return false

        val response = client.getIllustDetail(id)
        return register(response.illust, auto)
    }

    override suspend fun register(data: Illust, auto: Boolean): Boolean {
        val client = Stella.Pixiv ?: return false
        val downloader = PixivDownloader(client)

        val tags = data.tags.map { it.name }
        val firstImageUrl = data.metaSinglePage.originalImageUrl
            ?: data.metaPages.firstOrNull()?.original
            ?: data.metaPages.firstOrNull()?.imageUrls?.original
            ?: error("Illust (ID: ${data.id}) has no images.")

        val media = when (data.type) {
            "illust" -> downloader.downloadIllusts(data.id, firstImageUrl, data.pageCount)
            "ugoira" -> {
                listOf(
                    downloader.downloadUgoira(data.id, firstImageUrl, data.width, data.height)
                )
            }
            else -> TODO("Unsupported illistType: ${data.type} from ${data.id}")
        }

        val entry = PicRegistration(
            title = data.title,
            description = data.caption,
            url = "https://www.pixiv.net/artworks/${data.id}",
            author = PicEntry.Author(
                name = data.user.name,
                username = data.user.account,
                url = "https://www.pixiv.net/users/${data.user.id}",
                id = data.user.id.toString()
            ),

            tags = tags,
            platform = PicEntry.Platform.Pixiv,
            sensitiveLevel = when {
                "R-15" in tags -> PicEntry.SensitiveLevel.R15
                "R-18" in tags || data.xRestrict == 1 -> PicEntry.SensitiveLevel.R18
                "R-18G" in tags || data.xRestrict == 2 -> PicEntry.SensitiveLevel.R18G
                else -> PicEntry.SensitiveLevel.Safe
            },
            created = ZonedDateTime.parse(data.createDate, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant().toEpochMilli(),

            media = media,
            popularity = PicEntry.Popularity(
                bookmark = data.totalBookmarks,
                reply = data.totalComments,
                view = data.totalView,
                retweet = null,
                like = null
            )
        )

        MediaRegistory.register(entry, auto)

        if (Env.WATCH_THEN_FOLLOW_PIXIV) {
            client.addFollow(data.user.id, true)
        }

        return true
    }
}
