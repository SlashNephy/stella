package blue.starry.stella.worker.pixiv

import blue.starry.stella.Env
import blue.starry.stella.logger
import blue.starry.stella.models.PicEntry
import blue.starry.stella.register.MediaRegister
import blue.starry.stella.register.PicRegistration
import blue.starry.stella.worker.StellaPixivClient
import kotlinx.coroutines.*
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.time.Duration.Companion.minutes

object PixivSourceProvider {
    fun start() {
        val client = StellaPixivClient ?: return

        GlobalScope.launch {
            while (isActive) {
                try {
                    fetchBookmark(client, false)
                    fetchBookmark(client, true)
                } catch (e: CancellationException) {
                    break
                } catch (e: Throwable) {
                    client.logout()
                    logger.error(e) { "PixivSource で例外が発生しました。" }
                }

                delay(Env.CHECK_INTERVAL_MINS.minutes)
            }
        }
    }

    suspend fun fetch(client: PixivClient, url: String, auto: Boolean) {
        val id = url.split("=", "/").last().split("?").first().toInt()
        val response = client.getIllustDetail(id)

        register(client, response.illust, auto)
    }

    private suspend fun fetchBookmark(client: PixivClient, private: Boolean) {
        for (bookmark in client.getBookmarks(private).illusts.reversed()) {
            val response = client.getIllustDetail(bookmark.id)

            register(client, response.illust, false)
            client.deleteBookmark(bookmark.id)
        }
    }

    private suspend fun register(client: PixivClient, illust: PixivModel.IllustDetailResponse.Illust, auto: Boolean) {
        val downloader = PixivDownloader(client)

        val tags = illust.tags.map { it.name }
        val media = when (illust.type) {
            "illust" -> downloader.downloadIllusts(illust.id, illust.metaSinglePage.originalImageUrl, illust.pageCount)
            "ugoira" -> {
                listOf(
                    downloader.downloadUgoira(illust.id, illust.metaSinglePage.originalImageUrl, illust.width, illust.height)
                )
            }
            else -> TODO("Unsupported illistType: ${illust.type} from ${illust.id}")
        }

        val entry = PicRegistration(
            title = illust.title,
            description = illust.caption,
            url = "https://www.pixiv.net/artworks/${illust.id}",
            author = PicRegistration.Author(
                illust.user.name,
                "https://www.pixiv.net/users/${illust.id}",
                illust.user.account
            ),

            tags = tags,
            platform = PicEntry.Platform.Pixiv,
            sensitiveLevel = when {
                "R-15" in tags -> PicEntry.SensitiveLevel.R15
                "R-18" in tags || illust.xRestrict == 1 -> PicEntry.SensitiveLevel.R18
                "R-18G" in tags || illust.xRestrict == 2 -> PicEntry.SensitiveLevel.R18G
                else -> PicEntry.SensitiveLevel.Safe
            },
            created = ZonedDateTime.parse(illust.createDate, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant().toEpochMilli(),

            media = media,
            popularity = PicRegistration.Popularity(
                bookmark = illust.totalBookmarks,
                reply = illust.totalComments,
                view = illust.totalView
            )
        )

        MediaRegister.register(entry, auto)
    }
}
