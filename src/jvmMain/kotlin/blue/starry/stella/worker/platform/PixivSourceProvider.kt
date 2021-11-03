package blue.starry.stella.worker.platform

import blue.starry.stella.Env
import blue.starry.stella.logger
import blue.starry.stella.mediaDirectory
import blue.starry.stella.models.PicModel
import blue.starry.stella.worker.MediaRegister
import blue.starry.stella.worker.StellaPixivClient
import kotlinx.coroutines.*
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.time.Duration
import kotlin.time.minutes

object PixivSourceProvider {
    private val requestedIds = CopyOnWriteArrayList<Int>()

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

                delay(Duration.minutes(Env.CHECK_INTERVAL_MINS))
            }
        }
    }

    suspend fun enqueue(client: PixivClient, url: String) {
        val id = url.split("=", "/").last().toInt()
        if (id in requestedIds) {
            return
        }

        client.addBookmark(id, true)
        requestedIds += id
    }

    private suspend fun fetchBookmark(client: PixivClient, private: Boolean) {
        for (illust in client.getBookmarks(private).illusts.reversed()) {
            if (illust.id in requestedIds) {
                register(client, illust, null, true)
            } else {
                register(client, illust, "User", false)
            }

            client.deleteBookmark(illust.id)
        }
    }

    private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.ENGLISH)
    private suspend fun register(client: PixivClient, illust: PixivModel.Illust, user: String?, auto: Boolean) {
        val tags = illust.tags.map { it.name }

        val entry = MediaRegister.Entry(
            title = illust.title,
            description = illust.caption,
            url = "https://www.pixiv.net/artworks/${illust.id}",
            author = MediaRegister.Entry.Author(
                illust.user.name,
                "https://www.pixiv.net/users/${illust.user.id}",
                illust.user.account
            ),

            user = user,
            tags = tags,
            platform = PicModel.Platform.Pixiv,
            sensitiveLevel = when {
                "R-15" in tags -> PicModel.SensitiveLevel.R15
                "R-18" in tags -> PicModel.SensitiveLevel.R18
                "R-18G" in tags -> PicModel.SensitiveLevel.R18G
                else -> when (illust.xRestrict) {
                    0 -> PicModel.SensitiveLevel.Safe
                    1 -> PicModel.SensitiveLevel.R18
                    2 -> PicModel.SensitiveLevel.R18G
                    else -> PicModel.SensitiveLevel.R18
                }
            },
            created = ZonedDateTime.parse(illust.createDate, dateFormat).toInstant().toEpochMilli(),

            media = (illust.metaSinglePage.originalImageUrl?.let {
                listOf(it)
            }.orEmpty() + illust.metaPages.mapNotNull {
                it.imageUrls.original
            }).mapIndexed { index, url ->
                val ext = url.split(".").last().split("?").first()

                val file = mediaDirectory.resolve("pixiv_${illust.id}_$index.$ext").toFile()
                if (!file.exists()) {
                    client.download(url, file)
                }

                MediaRegister.Entry.Picture(index, "pixiv_${illust.id}_$index.$ext", url, ext)
            },
            popularity = MediaRegister.Entry.Popularity(
                // like = illust.li,
                bookmark = illust.totalBookmarks,
                // reply = illust.response,
                view = illust.totalView
            )
        )

        MediaRegister.register(entry, auto)
    }
}
