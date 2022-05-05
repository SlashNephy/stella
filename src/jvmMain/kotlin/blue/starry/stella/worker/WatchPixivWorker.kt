package blue.starry.stella.worker

import blue.starry.stella.Env
import blue.starry.stella.Stella
import blue.starry.stella.platforms.pixiv.PixivClient
import blue.starry.stella.platforms.pixiv.PixivSourceProvider
import blue.starry.stella.platforms.pixiv.entities.Illust
import io.ktor.util.error
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class WatchPixivWorker: Worker(Env.WATCH_INTERVAL_SECONDS.seconds) {
    override suspend fun run() {
        if (!Env.ENABLE_WATCH_PIXIV) {
            return
        }

        val client = Stella.Pixiv ?: return

        try {
            checkBookmarks(client, false)
            checkBookmarks(client, true)
        } catch (e: CancellationException) {
            throw e
        } catch (t: Throwable) {
            client.logout()
            logger.error(t)
        }
    }

    private suspend fun checkBookmarks(client: PixivClient, private: Boolean) = coroutineScope {
        val entries = client.getBookmarks(private).illusts.reversed()

        val jobs = entries.map { bookmark ->
            launch {
                checkBookmarksEach(client, bookmark)
            }
        }
        jobs.joinAll()
    }

    private suspend fun checkBookmarksEach(client: PixivClient, bookmark: Illust) {
        val response = client.getIllustDetail(bookmark.id)

        PixivSourceProvider.register(response.illust, false)
        client.deleteBookmark(bookmark.id)
    }
}
