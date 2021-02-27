package blue.starry.stella.worker

import blue.starry.stella.logger
import blue.starry.stella.models.PicModel
import blue.starry.stella.models.PicTagReplaceTableModel
import blue.starry.stella.worker.platform.NijieSourceProvider
import blue.starry.stella.worker.platform.PixivSourceProvider
import blue.starry.stella.worker.platform.TwitterSourceProvider
import org.litote.kmongo.coroutine.updateOne
import org.litote.kmongo.eq
import org.litote.kmongo.newId
import java.time.Instant

object MediaRegister {
    suspend fun registerByUrl(url: String, user: String?, auto: Boolean): Boolean {
        return runCatching {
            when {
                "twitter.com" in url -> {
                    TwitterSourceProvider.fetch(StellaTwitterClient ?: return false, url, user, auto)
                }
                "pixiv.net" in url -> {
                    PixivSourceProvider.enqueue(StellaPixivClient ?: return false, url)
                }
                "nijie.info" in url -> {
                    NijieSourceProvider.fetch(StellaNijieClient ?: return false, url, user, auto)
                }
                else -> return false
            }
        }.isSuccess
    }

    suspend fun register(entry: Entry, auto: Boolean) {
        val oldEntry = StellaMongoDBPicCollection.findOne(PicModel::url eq entry.url)
        val newEntry = PicModel(
            _id = oldEntry?._id ?: newId(),
            title = entry.title.normalizeTitle(),
            description = entry.description.normalizeDescription(),
            url = entry.url,
            tags = (entry.tags.map {
                PicModel.Tag(
                    value = it.normalizeTag(),
                    user = null,
                    locked = true
                )
            } + oldEntry?.tags?.map {
                it.copy(
                    value = it.value.normalizeTag()
                )
            }.orEmpty()).distinctBy { it.value },
            user = entry.user ?: oldEntry?.user,
            platform = entry.platform,
            sensitive_level = maxOf(entry.sensitiveLevel, oldEntry?.sensitive_level ?: 0),
            timestamp = PicModel.Timestamp(
                created = entry.created,
                added = oldEntry?.timestamp?.added ?: Instant.now().toEpochMilli(),
                auto_updated = if (auto) Instant.now().toEpochMilli() else (oldEntry?.timestamp?.auto_updated ?: Instant.now().toEpochMilli()),
                manual_updated = if (!auto) Instant.now().toEpochMilli() else (oldEntry?.timestamp?.manual_updated ?: Instant.now().toEpochMilli())
            ),
            author = PicModel.Author(
                name = entry.author.name,
                url = entry.author.url,
                username = entry.author.username
            ),
            media = entry.media.map {
                PicModel.Media(
                    index = it.index,
                    filename = it.filename,
                    original = it.filename,
                    ext = it.ext
                )
            },
            rating = PicModel.Rating(
                count = oldEntry?.rating?.count ?: 0,
                score = oldEntry?.rating?.score ?: 0
            ),
            popularity = PicModel.Popularity(
                like = entry.popularity.like,
                bookmark = entry.popularity.bookmark,
                view = entry.popularity.view,
                retweet = entry.popularity.retweet,
                reply = entry.popularity.reply
            )
        )

        if (oldEntry != null) {
            StellaMongoDBPicCollection.updateOne(newEntry)
            logger.info { "\"${entry.title}\" (${entry.url}) を更新しました。" }
        } else {
            StellaMongoDBPicCollection.insertOne(newEntry)
            logger.info { "\"${entry.title}\" (${entry.url}) を追加しました。" }
        }
    }

    private fun String.normalizeTitle(): String {
        return replace("\r\n", " ")
            .replace("\n", " ")
            .replace("<br>", " ")
    }

    private fun String.normalizeDescription(): String {
        return replace("\r\n", "<br>")
            .replace("\n", "<br>")
    }

    private suspend fun String.normalizeTag(): String {
        return StellaMongoDBPicTagReplaceTableCollection.findOne(PicTagReplaceTableModel::from eq this)?.to ?: this
    }

    data class Entry(val title: String, val description: String, val url: String, val tags: List<String>, val user: String?, val platform: String, val sensitiveLevel: Int, val created: Long, val author: Author, val media: List<Picture>, val popularity: Popularity) {
        data class Author(val name: String, val url: String, val username: String?)
        data class Picture(val index: Int, val filename: String, val original: String, val ext: String)
        data class Popularity(val like: Int? = null, val bookmark: Int? = null, val view: Int? = null, val retweet: Int? = null, val reply: Int? = null)
    }
}
