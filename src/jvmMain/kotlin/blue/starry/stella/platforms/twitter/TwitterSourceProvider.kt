package blue.starry.stella.platforms.twitter

import blue.starry.penicillin.endpoints.common.TweetMode
import blue.starry.penicillin.endpoints.friendships
import blue.starry.penicillin.endpoints.friendships.createByUserId
import blue.starry.penicillin.endpoints.statuses
import blue.starry.penicillin.endpoints.statuses.show
import blue.starry.penicillin.extensions.idObj
import blue.starry.penicillin.extensions.models.text
import blue.starry.penicillin.models.Status
import blue.starry.stella.Env
import blue.starry.stella.Stella
import blue.starry.stella.models.PicEntry
import blue.starry.stella.models.internal.MediaExtensionSerializer
import blue.starry.stella.platforms.SourceProvider
import blue.starry.stella.register.MediaRegistory
import blue.starry.stella.register.PicRegistration
import io.ktor.client.request.get
import io.ktor.http.ContentType

object TwitterSourceProvider: SourceProvider<Long, Status> {
    private val TweetUrlPattern = "^(?:https?://)?(?:m\\.|mobile\\.)?twitter\\.com/(?:\\w|_)+?/status/(?<id>\\d+)".toRegex()
    private val TcoUrlPattern = "https://t\\.co/[a-zA-Z0-9]+".toRegex()

    override suspend fun registerByUrl(url: String, auto: Boolean): Boolean {
        val match = TweetUrlPattern.find(url) ?: return false
        val id = match.groups["id"]!!.value.toLong()

        return registerById(id, auto)
    }

    override suspend fun registerById(id: Long, auto: Boolean): Boolean {
        val client = Stella.Twitter ?: return false
        val status = client.statuses.show(
            id = id,
            tweetMode = TweetMode.Extended
        ).execute().result

        return register(status, auto)
    }

    override suspend fun register(data: Status, auto: Boolean): Boolean {
        val media = data.extendedEntities?.media ?: data.entities.media
        if (media.isEmpty()) {
            return false
        }

        val entry = PicRegistration(
            title = "${data.text.take(20)}...",
            description = data.text.replace(TcoUrlPattern) {
                "<a href=\"${it.value}\">${it.value}</a>"
            },
            url = "https://twitter.com/${data.user.screenName}/status/${data.idStr}",
            tags = data.entities.hashtags.map { it.text },
            platform = PicEntry.Platform.Twitter,
            sensitiveLevel = when {
                "🔞" in data.text -> PicEntry.SensitiveLevel.R18
                data.possiblySensitive -> PicEntry.SensitiveLevel.R15
                else -> PicEntry.SensitiveLevel.Safe
            },
            created = data.idObj.epochTimeMillis,
            author = PicEntry.Author(
                name = data.user.name,
                url = "https://twitter.com/${data.user.screenName}",
                username = data.user.screenName,
                id = data.user.idStr
            ),
            media = media.mapIndexed { i, it ->
                val url = it.videoInfo?.variants?.filter {
                    it.contentType == ContentType.Video.MP4.toString()
                }?.maxByOrNull { it.bitrate ?: 0 }?.url
                    ?: it.videoInfo?.variants?.firstOrNull()?.url
                    ?: it.mediaUrlHttps
                val ext = MediaExtensionSerializer.deserializeFromUrl(url)

                val file = Stella.MediaDirectory.resolve("twitter_${data.idStr}_$i.$ext").toFile()
                if (!file.exists()) {
                    val response = Stella.Http.get<ByteArray>(url)
                    file.writeBytes(response)
                }

                PicEntry.Media(i, "twitter_${data.idStr}_$i.$ext", url, ext)
            },
            popularity = PicEntry.Popularity(
                like = data.favoriteCount,
                retweet = data.retweetCount,
                view = null,
                bookmark = null,
                reply = data.replyCount
            )
        )

        MediaRegistory.register(entry, auto)

        if (Env.WATCH_THEN_FOLLOW_TWITTER && !data.user.following) {
            val client = Stella.Twitter ?: return true
            client.friendships.createByUserId(userId = data.user.id).execute()
        }

        return true
    }
}
