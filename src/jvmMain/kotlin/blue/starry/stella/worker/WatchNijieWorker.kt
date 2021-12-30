package blue.starry.stella.worker

import blue.starry.stella.Env
import blue.starry.stella.Stella
import blue.starry.stella.platforms.nijie.NijieClient
import blue.starry.stella.platforms.nijie.NijieSourceProvider
import blue.starry.stella.platforms.nijie.models.Bookmark
import io.ktor.util.error
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.minutes

class WatchNijieWorker: Worker(Env.CHECK_INTERVAL_MINS.minutes) {
    override suspend fun run() {
        val client = Stella.Nijie ?: return

        try {
            checkBookmarks(client)
        } catch (e: CancellationException) {
            throw e
        } catch (t: Throwable) {
            client.logout()
            logger.error(t)
        }
    }

    private suspend fun checkBookmarks(client: NijieClient) {
        val entries = client.bookmarks().reversed()

        val jobs = entries.map { bookmark ->
            launch {
                checkBookmarksEach(client, bookmark)
            }
        }
        jobs.joinAll()
    }

    private suspend fun checkBookmarksEach(client: NijieClient, bookmark: Bookmark) {
        NijieSourceProvider.registerById(bookmark.id, false)
        client.deleteBookmark(bookmark.id)
    }
}
