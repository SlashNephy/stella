package blue.starry.stella.register

import blue.starry.stella.Env
import blue.starry.stella.Stella
import blue.starry.stella.models.PicEntry
import blue.starry.stella.platforms.nijie.NijieSourceProvider
import blue.starry.stella.platforms.pixiv.PixivSourceProvider
import blue.starry.stella.platforms.twitter.TwitterSourceProvider
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

    suspend fun registerByUrl(url: String, auto: Boolean): Boolean {
        for (provider in providers) {
            if (provider.registerByUrl(url, auto)) {
                return true
            }
        }

        return false
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
                    value = Normalizer.normalizeTag(it),
                    user = null,
                    locked = true
                )
            } + old?.tags?.map {
                it.copy(
                    value = Normalizer.normalizeTag(it.value)
                )
            }.orEmpty()).distinctBy { it.value },
            platform = entry.platform,
            sensitive_level = maxOf(entry.sensitiveLevel, old?.sensitive_level ?: PicEntry.SensitiveLevel.Safe),
            timestamp = PicEntry.Timestamp(
                created = entry.created,
                added = old?.timestamp?.added ?: Instant.now().toEpochMilli(),
                auto_updated = if (auto) Instant.now().toEpochMilli() else (old?.timestamp?.auto_updated ?: Instant.now().toEpochMilli()),
                manual_updated = if (!auto) Instant.now().toEpochMilli() else (old?.timestamp?.manual_updated ?: Instant.now().toEpochMilli()),
                archived = false
            ),
            author = entry.author,
            media = entry.media,
            rating = PicEntry.Rating(
                count = old?.rating?.count ?: 0,
                score = old?.rating?.score ?: 0
            ),
            popularity = entry.popularity
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
