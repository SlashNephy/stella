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
import java.util.concurrent.TimeUnit
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import javax.imageio.ImageIO
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.outputStream
import kotlin.io.path.writeBytes
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
        logger.debug { illust }

        val tags = illust.tags.map { it.name }
        val media = when (illust.type) {
            "illust" -> client.downloadIllusts(illust.id, illust.metaSinglePage.originalImageUrl, illust.pageCount)
            "ugoira" -> {
                listOf(
                    client.downloadUgoira(illust.id, illust.metaSinglePage.originalImageUrl, illust.width, illust.height)
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

    private suspend fun PixivClient.downloadIllust(id: Int, base_url: String, index: Int): PicRegistration.Picture {
        val extension = base_url.split(".").last().split("?").first()
        val filename = "pixiv_${id}_$index.$extension"

        val url = base_url.replace("_p0", "_p${index}")
        val path = mediaDirectory.resolve(filename)
        if (!path.exists()) {
            val image = download(url)
            path.writeBytes(image)
        }

        return PicRegistration.Picture(index, filename, url, extension)
    }

    private suspend fun PixivClient.downloadIllusts(id: Int, url: String, pages: Int): List<PicRegistration.Picture> {
        return (0 until pages).map { index ->
            downloadIllust(id, url, index)
        }
    }

    private suspend fun PixivClient.downloadUgoira(id: Int, url: String, width: Int, height: Int): PicRegistration.Picture {
        val filename = "pixiv_${id}_0.gif"

        val path = mediaDirectory.resolve(filename)
        if (!path.exists()) {
            val meta = getUgoiraMetadata(id).ugoiraMetadata
            val tmp = withContext(Dispatchers.IO) {
                Files.createTempFile("pixiv", "ugoira")
            }
            download(meta.zipUrls.medium, tmp)
            val zipFile = withContext(Dispatchers.IO) {
                ZipInputStream()
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
