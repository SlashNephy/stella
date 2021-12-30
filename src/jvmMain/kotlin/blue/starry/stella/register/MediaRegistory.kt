package blue.starry.stella.register

import blue.starry.stella.Env
import blue.starry.stella.Stella
import blue.starry.stella.models.PicEntry
import blue.starry.stella.platforms.nijie.NijieSourceProvider
import blue.starry.stella.platforms.pixiv.PixivSourceProvider
import blue.starry.stella.platforms.twitter.TwitterSourceProvider
import kotlinx.coroutines.CancellationException
import org.litote.kmongo.coroutine.updateOne
import org.litote.kmongo.eq
import org.litote.kmongo.newId
import java.time.Instant

object MediaRegistory {
    private val providers = listOf(
        TwitterSourceProvider,
        PixivSourceProvider,
        NijieSourceProvider
    )

    suspend fun registerByUrl(url: String, auto: Boolean) {
        for (provider in providers) {
            try {
                if (provider.registerByUrl(url, auto)) {
                    Stella.Logger.debug { "エントリー: $url を取得しました。" }
                    break
                }
            } catch (e: CancellationException) {
                throw e
            } catch (t: Throwable) {
                Stella.Logger.error(t) { "エントリー: $url の取得に失敗しました。" }
                throw t
            }
        }
    }

    suspend fun register(entry: PicRegistration, auto: Boolean) {
        val old = Stella.PicCollection.findOne(PicEntry::url eq entry.url)
        val new = PicEntry(
            _id = old?._id ?: newId(),
            title = Normalizer.normalizeTitle(entry.title),
            description = Normalizer.normalizeDescription(entry.description),
            url = entry.url,
            tags = (entry.tags.map {
                PicEntry.Tag(
                    value = Normalizer.normalizeTag(it), user = null, locked = true
                )
            } + old?.tags?.map {
                it.copy(
                    value = Normalizer.normalizeTag(it.value)
                )
            }.orEmpty()).distinctBy { it.value },
            user = old?.user,
            platform = entry.platform,
            sensitive_level = maxOf(entry.sensitiveLevel, old?.sensitive_level ?: PicEntry.SensitiveLevel.Safe),
            timestamp = PicEntry.Timestamp(
                created = entry.created,
                added = old?.timestamp?.added ?: Instant.now().toEpochMilli(),
                auto_updated = if (auto) Instant.now().toEpochMilli() else (old?.timestamp?.auto_updated ?: Instant.now().toEpochMilli()),
                manual_updated = if (!auto) Instant.now().toEpochMilli() else (old?.timestamp?.manual_updated ?: Instant.now().toEpochMilli()),
                archived = false
            ),
            author = PicEntry.Author(
                name = entry.author.name, url = entry.author.url, username = entry.author.username
            ),
            media = entry.media.map {
                PicEntry.Media(
                    index = it.index, filename = it.filename, original = it.filename, ext = it.ext
                )
            },
            rating = PicEntry.Rating(
                count = old?.rating?.count ?: 0, score = old?.rating?.score ?: 0
            ),
            popularity = PicEntry.Popularity(
                like = entry.popularity.like, bookmark = entry.popularity.bookmark, view = entry.popularity.view, retweet = entry.popularity.retweet, reply = entry.popularity.reply
            )
        )

        if (old != null) {
            if (!Env.DRYRUN) {
                Stella.PicCollection.updateOne(new)
            }

            Stella.Logger.info { "エントリー: \"${entry.title}\" (${entry.url}) を更新しました。" }
        } else {
            if (!Env.DRYRUN) {
                Stella.PicCollection.insertOne(new)
            }

            Stella.Logger.info { "エントリー: \"${entry.title}\" (${entry.url}) を追加しました。" }
        }
    }
}
