package blue.starry.stella.platforms.nijie

import blue.starry.stella.Env
import blue.starry.stella.Stella
import blue.starry.stella.models.PicEntry
import blue.starry.stella.models.internal.MediaExtensionSerializer
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
        val formatter = DateTimeFormatter.ofPattern("EEE MMM ppd HH:mm:ss yyyy", Locale.ENGLISH)
        val createdAt = LocalDateTime.parse(data.illust.datePublished, formatter).atZone(ZoneId.of("UTC")).toInstant().toEpochMilli()

        val reg = PicRegistration(
            title = data.illust.name,
            description = data.illust.description,
            url = data.url,
            tags = data.tags,
            platform = PicEntry.Platform.Nijie,
            sensitiveLevel = PicEntry.SensitiveLevel.R18,
            created = createdAt,
            author = PicEntry.Author(
                name = data.illust.author.name,
                username = null,
                url = data.illust.author.sameAs,
                id = data.userId
            ),
            media = data.mediaUrls.mapIndexed { index, url ->
                val ext = MediaExtensionSerializer.deserializeFromUrl(url)
                val path = Stella.MediaDirectory.resolve("nijie_${data.id}_$index.$ext")

                val client = Stella.Nijie ?: return false
                val image = client.download(url)
                path.writeBytes(image)

                PicEntry.Media(index, "nijie_${data.id}_$index.$ext", url, ext)
            },
            popularity = PicEntry.Popularity(
                like = data.like,
                bookmark = data.bookmark,
                reply = data.reply,
                view = data.view,
                retweet = null
            )
        )

        MediaRegistory.register(reg, auto)

        if (Env.WATCH_THEN_FOLLOW_NIJIE && !data.isFollowing) {
            val client = Stella.Nijie ?: return true
            val id = reg.author.id ?: return true

            client.follow(id)
        }

        return true
    }
}
