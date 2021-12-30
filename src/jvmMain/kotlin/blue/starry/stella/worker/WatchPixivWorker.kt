package blue.starry.stella.worker

import blue.starry.stella.Env
import blue.starry.stella.Stella
import blue.starry.stella.platforms.pixiv.PixivClient
import blue.starry.stella.platforms.pixiv.PixivSourceProvider
import io.ktor.util.error
import kotlinx.coroutines.CancellationException
import kotlin.time.Duration.Companion.minutes

class WatchPixivWorker: Worker(Env.CHECK_INTERVAL_MINS.minutes) {
    override suspend fun run() {
        val client = Stella.Pixiv ?: return

        try {
            fetchBookmark(client, false)
            fetchBookmark(client, true)
        } catch (e: CancellationException) {
            throw e
        } catch (t: Throwable) {
            client.logout()
            logger.error(t)
        }
    }

    private suspend fun fetchBookmark(client: PixivClient, private: Boolean) {
        for (bookmark in client.getBookmarks(private).illusts.reversed()) {
            val response = client.getIllustDetail(bookmark.id)

            PixivSourceProvider.register(response.illust, false)
            client.deleteBookmark(bookmark.id)
        }
    }
}
