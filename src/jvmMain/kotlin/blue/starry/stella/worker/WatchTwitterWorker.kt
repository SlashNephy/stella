package blue.starry.stella.worker

import blue.starry.penicillin.core.session.ApiClient
import blue.starry.penicillin.endpoints.common.TweetMode
import blue.starry.penicillin.endpoints.favorites
import blue.starry.penicillin.endpoints.favorites.destroy
import blue.starry.penicillin.endpoints.favorites.list
import blue.starry.penicillin.endpoints.statuses
import blue.starry.penicillin.endpoints.statuses.delete
import blue.starry.penicillin.endpoints.statuses.unretweet
import blue.starry.penicillin.endpoints.timeline
import blue.starry.penicillin.endpoints.timeline.userTimeline
import blue.starry.penicillin.models.Status
import blue.starry.stella.Env
import blue.starry.stella.Stella
import blue.starry.stella.platforms.twitter.TwitterSourceProvider
import io.ktor.util.error
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.minutes

class WatchTwitterWorker: Worker(Env.CHECK_INTERVAL_MINS.minutes) {
    override suspend fun run() {
        val client = Stella.Twitter ?: return

        try {
            checkTimeline(client)
            checkFavorites(client)
        } catch (e: CancellationException) {
            throw e
        } catch (t: Throwable) {
            logger.error(t)
        }
    }

    private suspend fun checkTimeline(client: ApiClient) {
        val timeline = client.timeline.userTimeline(
            tweetMode = TweetMode.Extended
        ).execute()

        val jobs = timeline.map { status ->
            launch {
                checkTimelineEach(client, status)
            }
        }
        jobs.joinAll()
    }

    private suspend fun checkTimelineEach(client: ApiClient, status: Status) {
        val retweet = status.retweetedStatus

        when {
            // RT を処理
            retweet != null -> {
                TwitterSourceProvider.register(retweet, false)

                client.statuses.unretweet(retweet.id).execute()
            }
            // ツイート URL を処理
            status.entities.urls.isEmpty() -> {
                val jobs = status.entities.urls.map { entity ->
                    launch {
                        TwitterSourceProvider.registerByUrl(entity.expandedUrl, false)
                    }
                }
                jobs.joinAll()

                client.statuses.delete(status.id).execute()
            }
        }
    }

    private suspend fun checkFavorites(client: ApiClient) {
        val favorites = client.favorites.list(
            options = arrayOf("tweet_mode" to TweetMode.Extended)
        ).execute()

        val jobs = favorites.map { status ->
            launch {
                checkFavoritesEach(client, status)
            }
        }
        jobs.joinAll()
    }

    private suspend fun checkFavoritesEach(client: ApiClient, status: Status) {
        TwitterSourceProvider.register(status, false)

        client.favorites.destroy(id = status.id).execute()
    }
}
