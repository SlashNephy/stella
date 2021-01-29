package blue.starry.stella.worker.platform

import blue.starry.stella.Config
import blue.starry.stella.logger
import blue.starry.stella.mediaDirectory
import blue.starry.stella.worker.MediaRegister
import io.ktor.client.features.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.minutes

object NijieSourceProvider {
    fun start(email: String, password: String) {
        GlobalScope.launch {
            while (true) {
                try {
                    fetchBookmark(email, password)
                } catch (e: ResponseException) {
                    // セッション切れ
                    if (NijieClient.isLoggedIn) {
                        login(email, password)
                    }
                } catch (e: Throwable) {
                    logger.error(e) { "NijieSource で例外が発生しました。" }
                }

                delay(Config.CheckIntervalMins.minutes)
            }
        }
    }

    private suspend fun fetchBookmark(email: String, password: String) {
        if (!NijieClient.isLoggedIn) {
            login(email, password)
        }

        for (bookmark in NijieClient.bookmarks().reversed()) {
            val picture = NijieClient.picture(bookmark.id)
            register(picture, "User", false)

            NijieClient.deleteBookmark(bookmark.id)
        }
    }

    private suspend fun login(email: String, password: String) {
        NijieClient.login(email, password)
        logger.info { "Nijie にログインしました。" }
    }

    suspend fun fetch(url: String, user: String?, auto: Boolean) {
        val picture = NijieClient.picture(url.split("=").last())
        register(picture, user, auto)
    }

    private suspend fun register(picture: NijieModel.Picture, user: String?, auto: Boolean): MediaRegister.Entry {
        return MediaRegister.Entry(
            title = picture.title,
            description = picture.description,
            url = picture.url,
            tags = picture.tags,
            author = MediaRegister.Entry.Author(picture.authorName, picture.authorUrl, null),

            user = user,
            platform = "Nijie",
            sensitiveLevel = 2,
            created = picture.createdAt,

            media = picture.media.mapIndexed { index, url ->
                val ext = url.split(".").last().split("?").first()

                val file = mediaDirectory.resolve("nijie_${picture.id}_$index.$ext").toFile()
                if (!file.exists()) {
                    NijieClient.download(url, file)
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
