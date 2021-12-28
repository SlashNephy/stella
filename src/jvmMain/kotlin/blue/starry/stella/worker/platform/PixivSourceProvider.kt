package blue.starry.stella.worker.platform

import blue.starry.stella.Env
import blue.starry.stella.logger
import blue.starry.stella.mediaDirectory
import blue.starry.stella.models.PicEntry
import blue.starry.stella.register.MediaRegister
import blue.starry.stella.register.PicRegistration
import blue.starry.stella.worker.StellaPixivClient
import com.squareup.gifencoder.FloydSteinbergDitherer
import com.squareup.gifencoder.GifEncoder
import com.squareup.gifencoder.ImageOptions
import com.squareup.gifencoder.KMeansQuantizer
import kotlinx.coroutines.*
import java.awt.image.BufferedImage
import java.nio.file.Files
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit
import java.util.zip.ZipFile
import javax.imageio.ImageIO
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.outputStream
import kotlin.time.Duration.Companion.minutes

object PixivSourceProvider {
    private val autoRefreshIds = CopyOnWriteArrayList<Int>()

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

    suspend fun enqueue(client: PixivClient, url: String) {
        val id = url.split("=", "/").last().toInt()
        if (id in autoRefreshIds) {
            return
        }

        client.addBookmark(id, true)
        autoRefreshIds += id
    }

    private suspend fun fetchBookmark(client: PixivClient, private: Boolean) {
        for (bookmark in client.getBookmarks(private).illusts.reversed()) {
            val illust = client.getIllust(bookmark.id)

            client.register(illust, bookmark.id in autoRefreshIds)
            client.deleteBookmark(bookmark.id)
        }
    }

    private suspend fun PixivClient.register(illust: PixivModel.Illust, auto: Boolean) {
        val tags = illust.tags.tags.map { it.tag }
        val media = when (illust.illustType) {
            0, 2 ->  downloadIllusts(illust.id, illust.urls.original, illust.pageCount)
//            2 -> {
//                listOf(
//                    downloadUgoira(illust.id, illust.urls.original, illust.width, illust.height)
//                )
//            }
            else -> TODO("Unsupported illistType: ${illust.illustType} from ${illust.illustId}")
        }

        val entry = PicRegistration(
            title = illust.illustTitle,
            description = illust.illustComment,
            url = "https://www.pixiv.net/artworks/${illust.illustId}",
            author = PicRegistration.Author(
                illust.userName,
                "https://www.pixiv.net/users/${illust.userId}",
                illust.userAccount
            ),

            tags = tags,
            platform = PicEntry.Platform.Pixiv,
            sensitiveLevel = when {
                "R-15" in tags -> PicEntry.SensitiveLevel.R15
                "R-18" in tags || illust.xRestrict == 1 -> PicEntry.SensitiveLevel.R18
                "R-18G" in tags || illust.xRestrict == 2 -> PicEntry.SensitiveLevel.R18G
                else -> PicEntry.SensitiveLevel.Safe
            },
            created = ZonedDateTime.parse(illust.uploadDate, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant().toEpochMilli(),

            media = media,
            popularity = PicRegistration.Popularity(
                like = illust.likeCount,
                bookmark = illust.bookmarkCount,
                reply = illust.commentCount + illust.responseCount + illust.imageResponseCount,
                view = illust.viewCount
            )
        )

        MediaRegister.register(entry, auto)
    }

    private suspend fun PixivClient.downloadIllust(id: String, base_url: String, index: Int): PicRegistration.Picture {
        val extension = base_url.split(".").last().split("?").first()
        val filename = "pixiv_${id}_$index.$extension"

        val url = base_url.replace("_p0", "_p${index}")
        val path = mediaDirectory.resolve(filename)
        if (!path.exists()) {
            download(url, path)
        }

        return PicRegistration.Picture(index, filename, url, extension)
    }

    private suspend fun PixivClient.downloadIllusts(id: String, url: String, pages: Int): List<PicRegistration.Picture> {
        return (0 until pages).map { index ->
            downloadIllust(id, url, index)
        }
    }

    private suspend fun PixivClient.downloadUgoira(id: String, url: String, width: Int, height: Int): PicRegistration.Picture {
        val filename = "pixiv_${id}_0.gif"

        val path = mediaDirectory.resolve(filename)
        if (!path.exists()) {
            val meta = getIllustUgoiraMeta(id)
            val tmp = withContext(Dispatchers.IO) {
                Files.createTempFile("pixiv", "ugoira")
            }
            download(meta.originalSrc, tmp)
            val zipFile = withContext(Dispatchers.IO) {
                ZipFile(tmp.toFile())
            }

            try {
                path.outputStream().use { stream ->
                    val gif = GifEncoder(stream, width, height, 0)
                    meta.frames.forEach {
                        val entry = zipFile.getEntry(it.file)
                        val image = ImageIO.read(zipFile.getInputStream(entry))
                        val rgbs = convertImageToArray(image)
                        val options = ImageOptions()
                            .setColorQuantizer(KMeansQuantizer.INSTANCE)
                            .setDitherer(FloydSteinbergDitherer.INSTANCE)
                            .setDelay(it.delay.toLong(), TimeUnit.MILLISECONDS)
                        gif.addImage(rgbs, options)
                    }
                    gif.finishEncoding()
                }
            } finally {
                withContext(Dispatchers.IO) {
                    zipFile.close()
                    tmp.deleteIfExists()
                }
            }
        }

        return PicRegistration.Picture(0, filename, url, "gif")
    }

    private fun convertImageToArray(bf: BufferedImage): Array<IntArray> {
        val width = bf.width
        val height = bf.height

        val data = IntArray(width * height)
        bf.getRGB(0, 0, width, height, data, 0, width)

        val rgbArray = Array(height) { IntArray(width) }
        for (i in 0 until height) {
            for (j in 0 until width) {
                rgbArray[i][j] = data[i * width + j]
            }
        }
        return rgbArray
    }
}
