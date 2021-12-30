package blue.starry.stella.worker

import blue.starry.stella.Env
import blue.starry.stella.Stella
import blue.starry.stella.platforms.nijie.NijieClient
import blue.starry.stella.platforms.nijie.NijieSourceProvider
import io.ktor.util.error
import kotlinx.coroutines.CancellationException
import kotlin.time.Duration.Companion.minutes

class WatchNijieWorker: Worker(Env.CHECK_INTERVAL_MINS.minutes) {
    override suspend fun run() {
        val client = Stella.Nijie ?: return

        try {
            fetchBookmark(client)
        } catch (e: CancellationException) {
            throw e
        } catch (t: Throwable) {
            client.logout()
            logger.error(t)
        }
    }

    private suspend fun fetchBookmark(client: NijieClient) {
        for (bookmark in client.bookmarks().reversed()) {
            NijieSourceProvider.registerById(bookmark.id, false)
            client.deleteBookmark(bookmark.id)
        }
    }
}
