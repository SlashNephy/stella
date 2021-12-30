package blue.starry.stella.platforms.nijie

import blue.starry.stella.Stella
import blue.starry.stella.models.PicEntry
import blue.starry.stella.platforms.SourceProvider
import blue.starry.stella.platforms.nijie.models.IllustMeta
import blue.starry.stella.register.MediaRegistory
import blue.starry.stella.register.PicRegistration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.io.path.writeBytes

object NijieSourceProvider: SourceProvider<String, IllustMeta> {
    private val UrlPattern = "^(?:https?://)?nijie\\.info/view\\.php\\?id=(?<id>\\d+)".toRegex()

    override suspend fun registerByUrl(url: String, auto: Boolean): Boolean {
        val match = UrlPattern.find(url) ?: return false
        val id = match.groups["id"]!!.value

        return registerById(id, auto)
    }

    override suspend fun registerById(id: String, auto: Boolean): Boolean {
        val client = Stella.Nijie ?: return false

        val picture = client.getIllustMeta(id)
        return register(picture, auto)
    }

    override suspend fun register(data: IllustMeta, auto: Boolean): Boolean {
        val formatter = DateTimeFormatter.ofPattern("EEE MMM d HH:mm:ss yyyy", Locale.ENGLISH)
        val createdAt = LocalDateTime.parse(data.illust.datePublished, formatter).atZone(ZoneId.of("UTC")).toInstant().toEpochMilli()

        val reg = PicRegistration(
            title = data.illust.name,
            description = data.illust.description,
            url = data.url,
            tags = data.tags,
            platform = PicEntry.Platform.Nijie,
            sensitiveLevel = PicEntry.SensitiveLevel.R18,
            created = createdAt,
            author = PicRegistration.Author(data.illust.author.name, data.illust.author.sameAs, null),
            media = data.mediaUrls.mapIndexed { index, url ->
                val ext = url.split(".").last().split("?").first()
                val path = Stella.MediaDirectory.resolve("nijie_${data.id}_$index.$ext")

                val client = Stella.Nijie ?: return false
                val image = client.download(url)
                path.writeBytes(image)

                PicRegistration.Picture(index, "nijie_${data.id}_$index.$ext", url, ext)
            },
            popularity = PicRegistration.Popularity(
                like = data.like,
                bookmark = data.bookmark,
                reply = data.reply,
                view = data.view
            )
        )

        MediaRegistory.register(reg, auto)
        return true
    }
}
