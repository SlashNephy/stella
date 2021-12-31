package blue.starry.stella.platforms.pixiv

import blue.starry.stella.Stella
import blue.starry.stella.models.PicEntry
import blue.starry.stella.models.internal.MediaExtensionSerializer
import com.squareup.gifencoder.FloydSteinbergDitherer
import com.squareup.gifencoder.GifEncoder
import com.squareup.gifencoder.ImageOptions
import com.squareup.gifencoder.KMeansQuantizer
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit
import java.util.zip.ZipInputStream
import javax.imageio.ImageIO
import kotlin.io.path.writeBytes

class PixivDownloader(private val client: PixivClient) {
    suspend fun downloadIllusts(id: Int, url: String, pages: Int): List<PicEntry.Media> {
        return (0 until pages).map { index ->
            downloadIllust(id, url, index)
        }
    }

    private suspend fun downloadIllust(id: Int, base_url: String, index: Int): PicEntry.Media {
        val extension = MediaExtensionSerializer.deserializeFromUrl(base_url)

        val filename = "pixiv_${id}_$index.$extension"
        val url = base_url.replace("_p0", "_p${index}")
        val image = client.download(url)
        val path = Stella.MediaDirectory.resolve(filename)
        path.writeBytes(image)

        return PicEntry.Media(index, filename, url, extension)
    }

    suspend fun downloadUgoira(id: Int, url: String, width: Int, height: Int): PicEntry.Media {
        val filename = "pixiv_${id}_0.gif"
        val meta = client.getUgoiraMetadata(id).ugoiraMetadata
        val zipUrl = meta.zipUrls.medium.replace("_ugoira600x600", "_ugoira1920x1080")
        val zip = loadRemoteZip(zipUrl)
        val output = ByteArrayOutputStream()
        val gif = GifEncoder(output, width, height, 0)

        meta.frames.forEach {
            val entry = zip.getValue(it.file)
            val image = ImageIO.read(entry.inputStream())
            val rgbs = image.toRGBArray()
            val options = ImageOptions().setColorQuantizer(KMeansQuantizer.INSTANCE).setDitherer(FloydSteinbergDitherer.INSTANCE).setDelay(it.delay.toLong(), TimeUnit.MILLISECONDS)
            gif.addImage(rgbs, options)
        }

        gif.finishEncoding()
        val path = Stella.MediaDirectory.resolve(filename)
        path.writeBytes(output.toByteArray())

        return PicEntry.Media(0, filename, url, PicEntry.MediaExtension.gif)
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
