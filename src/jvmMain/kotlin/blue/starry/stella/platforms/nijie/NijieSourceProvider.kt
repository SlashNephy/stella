package blue.starry.stella.platforms.nijie

import blue.starry.stella.Stella
import blue.starry.stella.models.PicEntry
import blue.starry.stella.platforms.SourceProvider
import blue.starry.stella.platforms.nijie.models.Picture
import blue.starry.stella.register.MediaRegistory
import blue.starry.stella.register.PicRegistration

object NijieSourceProvider: SourceProvider<String, Picture> {
    private val UrlPattern = "^(?:https?://)?nijie\\.info/view\\.php\\?id=(?<id>\\d+)".toRegex()

    override suspend fun registerByUrl(url: String, auto: Boolean): Boolean {
        val match = UrlPattern.find(url) ?: return false
        val id = match.groups["id"]!!.value

        return registerById(id, auto)
    }

    override suspend fun registerById(id: String, auto: Boolean): Boolean {
        val client = Stella.Nijie ?: return false

        val picture = client.picture(id)
        return register(picture, auto)
    }

    override suspend fun register(data: Picture, auto: Boolean): Boolean {
        val reg = PicRegistration(
            title = data.title,
            description = data.description,
            url = data.url,
            tags = data.tags,
            platform = PicEntry.Platform.Nijie,
            sensitiveLevel = PicEntry.SensitiveLevel.R18,
            created = data.createdAt,
            author = PicRegistration.Author(data.authorName, data.authorUrl, null),
            media = data.media.mapIndexed { index, url ->
                val ext = url.split(".").last().split("?").first()
                val file = Stella.MediaDirectory.resolve("nijie_${data.id}_$index.$ext").toFile()
                if (!file.exists()) {
                    val client = Stella.Nijie ?: return false
                    client.download(url, file)
                }

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
