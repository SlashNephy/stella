package blue.starry.stella.worker.platform

import blue.starry.penicillin.core.session.ApiClient
import blue.starry.penicillin.endpoints.common.TweetMode
import blue.starry.penicillin.endpoints.favorites
import blue.starry.penicillin.endpoints.favorites.create
import blue.starry.penicillin.endpoints.favorites.destroy
import blue.starry.penicillin.endpoints.favorites.list
import blue.starry.penicillin.endpoints.friendships
import blue.starry.penicillin.endpoints.friendships.createByUserId
import blue.starry.penicillin.endpoints.statuses
import blue.starry.penicillin.endpoints.statuses.delete
import blue.starry.penicillin.endpoints.statuses.show
import blue.starry.penicillin.endpoints.statuses.unretweet
import blue.starry.penicillin.endpoints.timeline
import blue.starry.penicillin.endpoints.timeline.userTimeline
import blue.starry.penicillin.extensions.idObj
import blue.starry.penicillin.extensions.models.text
import blue.starry.penicillin.models.Status
import blue.starry.stella.Env
import blue.starry.stella.logger
import blue.starry.stella.mediaDirectory
import blue.starry.stella.models.PicModel
import blue.starry.stella.worker.MediaRegister
import blue.starry.stella.worker.StellaHttpClient
import blue.starry.stella.worker.StellaTwitterClient
import io.ktor.client.request.*
import kotlinx.coroutines.*
import kotlin.time.Duration
import kotlin.time.minutes

object TwitterSourceProvider {
    private val tweetUrlPattern = "^(?:https?://)?(?:m|mobile)?twitter\\.com/(?:\\w|_)+?/status/(\\d+)".toRegex()
    private val tcoUrlPattern = "https://t\\.co/[a-zA-Z0-9]+".toRegex()

    fun start() {
        val client = StellaTwitterClient ?: return

        GlobalScope.launch {
            while (isActive) {
                try {
                    fetchTimeline(client)
                    fetchFavorites(client)
                } catch (e: CancellationException) {
                    break
                } catch (e: Throwable) {
                    logger.error(e) { "TwitterSource で例外が発生しました。" }
                }

                delay(Duration.minutes(Env.CHECK_INTERVAL_MINS))
            }
        }
    }

    private suspend fun fetchTimeline(client: ApiClient) {
        val timeline = client.timeline.userTimeline().execute()

        for (status in timeline) {
            if (status.retweetedStatus != null) {
                // RT を処理
                register(status.retweetedStatus!!, "User", false)

                client.statuses.unretweet(status.retweetedStatus!!.id).execute()
            } else if (status.entities.urls.isEmpty()) {
                // ツイート URL を処理
                val tweet = status.retweetedStatus ?: status

                for (entity in tweet.entities.urls) {
                    val match = tweetUrlPattern.find(entity.expandedUrl) ?: continue
                    client.favorites.create(id = match.groupValues[1].toLong()).execute()
                }

                client.statuses.delete(status.id).execute()
            }
        }
    }

    private suspend fun fetchFavorites(client: ApiClient) {
        val favorites = client.favorites.list(options = arrayOf("tweet_mode" to TweetMode.Extended)).execute()
        for (status in favorites) {
            register(status, "User", false)

            if (!status.user.following) {
                client.friendships.createByUserId(userId = status.user.id).execute()
            }

            client.favorites.destroy(id = status.id).execute()
        }
    }

    suspend fun fetch(client: ApiClient, url: String, user: String?, auto: Boolean) {
        val status = client.statuses.show(
            id = url.split("/").last().split("?").first().toLong(),
            tweetMode = TweetMode.Extended
        ).execute()

        register(status.result, user, auto)
    }


    private suspend fun register(status: Status, user: String?, auto: Boolean) {
        val media = status.extendedEntities?.media ?: status.entities.media
        if (media.isEmpty()) {
            return
        }

        val entry = MediaRegister.Entry(
            title = "${status.text.take(20)}...",
            description = status.text.replace(tcoUrlPattern) {
                "<a href=\"${it.value}\">${it.value}</a>"
            },
            url = "https://twitter.com/${status.user.screenName}/status/${status.idStr}",
            author = MediaRegister.Entry.Author(status.user.name, "https://twitter.com/${status.user.screenName}", status.user.screenName),
            tags = status.entities.hashtags.map { it.text },

            user = user,
            platform = PicModel.Platform.Twitter,
            sensitiveLevel = when {
                "🔞" in status.text -> PicModel.SensitiveLevel.R18
                status.possiblySensitive -> PicModel.SensitiveLevel.R15
                else -> PicModel.SensitiveLevel.Safe
            },
            created = status.idObj.epochTimeMillis,

            media = media.mapIndexed { i, it ->
                val url = it.videoInfo?.variants?.firstOrNull()?.url ?: it.mediaUrlHttps
                val ext = url.split(".").last().split("?").first()

                val file = mediaDirectory.resolve("twitter_${status.idStr}_$i.$ext").toFile()
                if (!file.exists()) {
                    val response = StellaHttpClient.get<ByteArray>(url)
                    file.writeBytes(response)
                }

                MediaRegister.Entry.Picture(i, "twitter_${status.idStr}_$i.$ext", url, ext)
            },
            popularity = MediaRegister.Entry.Popularity(like = status.favoriteCount, retweet = status.retweetCount)
        )

        MediaRegister.register(entry, auto)
    }
}
