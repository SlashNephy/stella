package blue.starry.stella.worker.platform

import blue.starry.stella.config
import blue.starry.stella.logger
import blue.starry.stella.mediaDirectory
import blue.starry.stella.worker.MediaRegister
import io.ktor.client.features.ResponseException
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime
import kotlin.time.minutes

object NijieSourceProvider {
    @KtorExperimentalAPI
    @ExperimentalTime
    fun start() {
        GlobalScope.launch {
            while (true) {
                try {
                    fetchBookmark()

                    delay(1.minutes)
                } catch (e: ResponseException) {
                    // セッション切れ
                    if (NijieClient.isLoggedIn) {
                        login()
                    }
                } catch (e: Throwable) {
                    logger.error(e) { "NijieSource で例外が発生しました。" }
                }
            }
        }
    }

    @KtorExperimentalAPI
    private suspend fun fetchBookmark() {
        if (!NijieClient.isLoggedIn) {
            login()
        }

        for (bookmark in NijieClient.bookmarks().reversed()) {
            val picture = NijieClient.picture(bookmark.id)
            register(picture, "Nep", false)

            NijieClient.deleteBookmark(bookmark.id)
        }
    }

    @KtorExperimentalAPI
    private suspend fun login() {
        NijieClient.login(config.property("accounts.nijie.email").getString(), config.property("accounts.nijie.password").getString())
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
