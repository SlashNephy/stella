package blue.starry.stella.worker.platform

import blue.starry.penicillin.PenicillinClient
import blue.starry.penicillin.core.session.config.account
import blue.starry.penicillin.core.session.config.application
import blue.starry.penicillin.core.session.config.token
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
import blue.starry.penicillin.extensions.execute
import blue.starry.penicillin.extensions.idObj
import blue.starry.penicillin.extensions.models.text
import blue.starry.penicillin.models.Status
import blue.starry.stella.config
import blue.starry.stella.logger
import blue.starry.stella.mediaDirectory
import blue.starry.stella.worker.MediaRegister
import io.ktor.client.request.get
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime
import kotlin.time.minutes

@KtorExperimentalAPI
object TwitterSourceProvider {
    private val client = PenicillinClient {
        account {
            application(config.property("accounts.twitter.consumerKey").getString(), config.property("accounts.twitter.consumerSecret").getString())
            token(config.property("accounts.twitter.accessToken").getString(), config.property("accounts.twitter.accessTokenSecret").getString())
        }
    }
    private val tweetUrlPattern = "^(?:http(?:s)?://)?(?:m|mobile)?twitter\\.com/(?:\\w|_)+?/status/(\\d+)".toRegex()

    @ExperimentalTime
    fun start() {
        GlobalScope.launch {
            while (true) {
                try {
                    fetchTimeline()
                    fetchFavorites()

                    delay(1.minutes)
                } catch (e: Throwable) {
                    logger.error(e) { "TwitterSource で例外が発生しました。" }
                }
            }
        }
    }

    private suspend fun fetchTimeline() {
        val timeline = client.timeline.userTimeline().execute()
        for (status in timeline) {
            if (status.retweetedStatus != null) {
                // RT を処理
                register(status.retweetedStatus!!, "Nep", false)

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

    private suspend fun fetchFavorites() {
        val favorites = client.favorites.list(options = *arrayOf("tweet_mode" to TweetMode.Extended)).execute()
        for (status in favorites) {
            register(status, "Nep", false)

            if (!status.user.following) {
                client.friendships.createByUserId(userId = status.user.id).execute()
            }
            client.favorites.destroy(id = status.id).execute()
        }
    }

    suspend fun fetch(url: String, user: String?, auto: Boolean) {
        val status = client.statuses.show(url.split("/").last().split("?").first().toLong(), options = *arrayOf("tweet_mode" to TweetMode.Extended)).execute().result

        register(status, user, auto)
    }

    private val tcoRegex = "https://t\\.co/[a-zA-Z0-9]+".toRegex()
    private suspend fun register(status: Status, user: String?, auto: Boolean) {
        val media = status.extendedEntities?.media ?: status.entities.media
        if (media.isEmpty()) {
            return
        }

        val entry = MediaRegister.Entry(
            title = "${status.text.take(20)}...",
            description = status.text.replace(tcoRegex) {
                "<a href=\"${it.value}\">${it.value}</a>"
            },
            url = "https://twitter.com/${status.user.screenName}/status/${status.idStr}",
            author = MediaRegister.Entry.Author(status.user.name, "https://twitter.com/${status.user.screenName}", status.user.screenName),
            tags = status.entities.hashtags.map { it.text },

            user = user,
            platform = "Twitter",
            sensitiveLevel = if ("🔞" in status.text) 2 else if (status.possiblySensitive) 1 else 0,
            created = status.idObj.epochTimeMillis,

            media = media.mapIndexed { i, it ->
                val url = it.videoInfo?.variants?.firstOrNull()?.url ?: it.mediaUrlHttps
                val ext = url.split(".").last().split("?").first()

                val file = mediaDirectory.resolve("twitter_${status.idStr}_$i.$ext").toFile()
                if (!file.exists()) {
                    file.outputStream().use {
                        val response = client.session.httpClient.get<ByteArray>(url)
                        it.write(response)
                    }
                }

                MediaRegister.Entry.Picture(i, "twitter_${status.idStr}_$i.$ext", url, ext)
            },
            popularity = MediaRegister.Entry.Popularity(like = status.favoriteCount, retweet = status.retweetCount)
        )

        MediaRegister.register(entry, auto)
    }
}
