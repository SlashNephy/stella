package blue.starry.stella.worker.platform

import blue.starry.stella.Config
import blue.starry.stella.logger
import blue.starry.stella.mediaDirectory
import blue.starry.stella.worker.MediaRegister
import io.ktor.client.features.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.apache.commons.lang3.time.FastDateFormat
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.time.minutes

object PixivSourceProvider {
    private val requestedIds = CopyOnWriteArrayList<Int>()

    fun start() {
        GlobalScope.launch {
            while (true) {
                try {
                    fetchBookmark(false)
                    fetchBookmark(true)
                } catch (e: ResponseException) {
                    PixivClient.logout()
                } catch (e: Throwable) {
                    logger.error(e) { "PixivSource で例外が発生しました。" }
                }

                delay(Config.CheckIntervalMins.minutes)
            }
        }
    }

    suspend fun enqueue(url: String) {
        val id = url.split("=", "/").last().toInt()
        if (id in requestedIds) {
            return
        }

        PixivClient.addBookmark(id, true)
        requestedIds += id
    }

    private suspend fun fetchBookmark(private: Boolean) {
        for (illust in PixivClient.getBookmarks(private).illusts.reversed()) {
            if (illust.id in requestedIds) {
                register(illust, null, true)
            } else {
                register(illust, "User", false)
            }

            PixivClient.deleteBookmark(illust.id)
        }
    }

    private val dateFormat = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.ENGLISH)
    private suspend fun register(illust: PixivModel.Illust, user: String?, auto: Boolean) {
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
            platform = "Pixiv",
            sensitiveLevel = when {
                "R-15" in tags -> 1
                "R-18" in tags -> 2
                "R-18G" in tags -> 3
                else -> when (illust.xRestrict) {
                    0 -> 0
                    1 -> 2
                    2 -> 3
                    else -> 1
                }
            },
            created = dateFormat.parse(illust.createDate).time,

            media = (illust.metaSinglePage.originalImageUrl?.let {
                listOf(it)
            }.orEmpty() + illust.metaPages.mapNotNull {
                it.imageUrls.original
            }).mapIndexed { index, url ->
                val ext = url.split(".").last().split("?").first()

                val file = mediaDirectory.resolve("pixiv_${illust.id}_$index.$ext").toFile()
                if (!file.exists()) {
                    PixivClient.download(url, file)
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
