package blue.starry.stella.worker

import blue.starry.stella.Env
import blue.starry.stella.Stella
import blue.starry.stella.platforms.pixiv.PixivClient
import blue.starry.stella.platforms.pixiv.PixivSourceProvider
import blue.starry.stella.platforms.pixiv.entities.Illust
import io.ktor.util.error
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.minutes

class WatchPixivWorker: Worker(Env.CHECK_INTERVAL_MINS.minutes) {
    override suspend fun run() {
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

    private suspend fun checkBookmarks(client: PixivClient, private: Boolean) {
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
