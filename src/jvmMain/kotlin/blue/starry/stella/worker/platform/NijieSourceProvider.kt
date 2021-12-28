package blue.starry.stella.worker.platform

import blue.starry.stella.Env
import blue.starry.stella.logger
import blue.starry.stella.mediaDirectory
import blue.starry.stella.models.PicEntry
import blue.starry.stella.worker.MediaRegister
import blue.starry.stella.worker.StellaNijieClient
import kotlinx.coroutines.*
import kotlin.time.Duration.Companion.minutes

object NijieSourceProvider {
    fun start() {
        val client = StellaNijieClient ?: return

        GlobalScope.launch {
            while (isActive) {
                try {
                    fetchBookmark(client)
                } catch (e: CancellationException) {
                    break
                } catch (e: Throwable) {
                    client.logout()
                    logger.error(e) { "NijieSource で例外が発生しました。" }
                }

                delay(Env.CHECK_INTERVAL_MINS.minutes)
            }
        }
    }

    private suspend fun fetchBookmark(client: NijieClient) {
        for (bookmark in client.bookmarks().reversed()) {
            val picture = client.picture(bookmark.id)
            register(client, picture, false)

            client.deleteBookmark(bookmark.id)
        }
    }

    suspend fun fetch(client: NijieClient, url: String, auto: Boolean) {
        val picture = client.picture(url.split("=").last())
        register(client, picture, auto)
    }

    private suspend fun register(client: NijieClient, picture: NijieModel.Picture, auto: Boolean): MediaRegister.Entry {
        return MediaRegister.Entry(
            title = picture.title,
            description = picture.description,
            url = picture.url,
            tags = picture.tags,
            platform = PicEntry.Platform.Nijie,
            sensitiveLevel = PicEntry.SensitiveLevel.R18,
            created = picture.createdAt,
            author = MediaRegister.Entry.Author(picture.authorName, picture.authorUrl, null),
            media = picture.media.mapIndexed { index, url ->
                val ext = url.split(".").last().split("?").first()

                val file = mediaDirectory.resolve("nijie_${picture.id}_$index.$ext").toFile()
                if (!file.exists()) {
                    client.download(url, file)
                }

                MediaRegister.Entry.Picture(index, "nijie_${picture.id}_$index.$ext", url, ext)
            },
            popularity = MediaRegister.Entry.Popularity(
                like = picture.like,
                bookmark = picture.bookmark,
                reply = picture.reply,
                view = picture.view
            )
        ).also {
            MediaRegister.register(it, auto)
        }
    }
}
