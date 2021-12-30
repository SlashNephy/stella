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
import blue.starry.stella.Env
import blue.starry.stella.Stella
import blue.starry.stella.platforms.twitter.TwitterSourceProvider
import io.ktor.util.error
import io.ktor.utils.io.CancellationException
import kotlin.time.Duration.Companion.minutes

class WatchTwitterWorker: Worker(Env.CHECK_INTERVAL_MINS.minutes) {
    override suspend fun run() {
        val client = Stella.Twitter ?: return

        try {
            fetchTimeline(client)
            fetchFavorites(client)
        } catch (e: CancellationException) {
            throw e
        } catch (t: Throwable) {
            logger.error(t)
        }
    }

    private suspend fun fetchTimeline(client: ApiClient) {
        val timeline = client.timeline.userTimeline(
            tweetMode = TweetMode.Extended
        ).execute()

        for (status in timeline) {
            val retweet = status.retweetedStatus

            when {
                // RT を処理
                retweet != null -> {
                    TwitterSourceProvider.register(retweet, false)

                    client.statuses.unretweet(retweet.id).execute()
                }
                // ツイート URL を処理
                status.entities.urls.isEmpty() -> {
                    for (entity in status.entities.urls) {
                        val match = TwitterSourceProvider.TweetUrlPattern.find(entity.expandedUrl) ?: continue

                        val id = match.groupValues[1].toLong()
                        TwitterSourceProvider.registerById(id, false)
                    }

                    client.statuses.delete(status.id).execute()
                }
            }
        }
    }

    private suspend fun fetchFavorites(client: ApiClient) {
        val favorites = client.favorites.list(
            options = arrayOf("tweet_mode" to TweetMode.Extended)
        ).execute()

        for (status in favorites) {
            TwitterSourceProvider.register(status, false)

            client.favorites.destroy(id = status.id).execute()
        }
    }
}
