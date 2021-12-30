package blue.starry.stella.worker.pixiv

import blue.starry.stella.mediaDirectory
import blue.starry.stella.register.PicRegistration
import com.squareup.gifencoder.FloydSteinbergDitherer
import com.squareup.gifencoder.GifEncoder
import com.squareup.gifencoder.ImageOptions
import com.squareup.gifencoder.KMeansQuantizer
import java.awt.image.BufferedImage
import java.util.concurrent.TimeUnit
import java.util.zip.ZipInputStream
import javax.imageio.ImageIO
import kotlin.io.path.exists
import kotlin.io.path.outputStream
import kotlin.io.path.writeBytes

class PixivDownloader(private val client: PixivClient) {
    suspend fun downloadIllusts(id: Int, url: String, pages: Int): List<PicRegistration.Picture> {
        return (0 until pages).map { index ->
            downloadIllust(id, url, index)
        }
    }

    private suspend fun downloadIllust(id: Int, base_url: String, index: Int): PicRegistration.Picture {
        val extension = base_url.split(".").last().split("?").first()
        val filename = "pixiv_${id}_$index.$extension"

        val url = base_url.replace("_p0", "_p${index}")
        val path = mediaDirectory.resolve(filename)
        if (!path.exists()) {
            val image = client.download(url)
            path.writeBytes(image)
        }

        return PicRegistration.Picture(index, filename, url, extension)
    }

    suspend fun downloadUgoira(id: Int, url: String, width: Int, height: Int): PicRegistration.Picture {
        val filename = "pixiv_${id}_0.gif"

        val path = mediaDirectory.resolve(filename)
        if (!path.exists()) {
            val meta = client.getUgoiraMetadata(id).ugoiraMetadata
            val zipUrl = meta.zipUrls.medium.replace("_ugoira600x600", "_ugoira1920x1080")
            val zip = loadRemoteZip(zipUrl)

            path.outputStream().use { stream ->
                val gif = GifEncoder(stream, width, height, 0)

                meta.frames.forEach {
                    val entry = zip.getValue(it.file)

                    val image = ImageIO.read(entry.inputStream())
                    val rgbs = image.toRGBArray()
                    val options = ImageOptions()
                        .setColorQuantizer(KMeansQuantizer.INSTANCE)
                        .setDitherer(FloydSteinbergDitherer.INSTANCE)
                        .setDelay(it.delay.toLong(), TimeUnit.MILLISECONDS)
                    gif.addImage(rgbs, options)
                }
                gif.finishEncoding()
            }
        }

        return PicRegistration.Picture(0, filename, url, "gif")
    }

    private suspend fun loadRemoteZip(url: String): Map<String, ByteArray> {
        val content = client.download(url)
        val stream = content.inputStream()

        return ZipInputStream(stream).use { zis ->
            buildMap {
                while (true) {
                    val entry = zis.nextEntry ?: break
                    val bytes = zis.readBytes()

                    put(entry.name, bytes)
                }
            }
        }
    }

    private fun BufferedImage.toRGBArray(): Array<IntArray> {
        val width = width
        val height = height

        val data = IntArray(width * height)
        getRGB(0, 0, width, height, data, 0, width)

        val rgbArray = Array(height) { IntArray(width) }
        for (i in 0 until height) {
            for (j in 0 until width) {
                rgbArray[i][j] = data[i * width + j]
            }
        }
        return rgbArray
    }
}
