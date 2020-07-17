package blue.starry.stella.worker

import blue.starry.jsonkt.jsonObjectOf
import blue.starry.jsonkt.stringify
import blue.starry.stella.api.toPic
import blue.starry.stella.api.toTagReplaceTable
import blue.starry.stella.collection
import blue.starry.stella.logger
import blue.starry.stella.tagReplaceTable
import blue.starry.stella.worker.platform.NijieSourceProvider
import blue.starry.stella.worker.platform.PixivSourceProvider
import blue.starry.stella.worker.platform.TwitterSourceProvider
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import org.bson.Document
import java.util.*

object MediaRegister {
    suspend fun registerByUrl(url: String, user: String?, auto: Boolean): Boolean {
        return runCatching {
            @Suppress("IMPLICIT_CAST_TO_ANY")
            when {
                "twitter.com" in url -> {
                    TwitterSourceProvider.fetch(url, user, auto)
                }
                "pixiv.net" in url -> {
                    PixivSourceProvider.enqueue(url)
                }
                "nijie.info" in url -> {
                    NijieSourceProvider.fetch(url, user, auto)
                }
                else -> return false
            }
        }.isSuccess
    }

    suspend fun register(entry: Entry, auto: Boolean) {
        val oldEntry = collection.findOne(Filters.eq("url", entry.url))?.toPic()

        val title = entry.title.replace("\r\n", " ").replace("\n", " ").replace("<br>", " ")
        val description = entry.description.replace("\r\n", "<br>").replace("\n", "<br>")
        if (oldEntry != null) {
            val tags = entry.tags.map {
                mapOf(
                    "value" to (tagReplaceTable.findOne(Filters.eq("from", it))?.toTagReplaceTable()?.to ?: it),
                    "user" to null,
                    "locked" to true
                )
            } + oldEntry.tags.filter { !it.locked && it.value !in entry.tags }.map {
                mapOf(
                    "value" to (tagReplaceTable.findOne(Filters.eq("from", it.value))?.toTagReplaceTable()?.to ?: it.value),
                    "user" to it.user,
                    "locked" to it.locked
                )
            }

            collection.updateOne(
                Filters.eq("url", entry.url),
                Updates.combine(
                    Updates.set("title", title),
                    Updates.set("description", description),
                    Updates.set("tags", tags.distinctBy { it["value"] }),
                    Updates.set("user", entry.user ?: oldEntry.user),
                    Updates.set("sensitive_level", maxOf(entry.sensitiveLevel, oldEntry.sensitiveLevel)),
                    Updates.set("timestamp.${if (auto) "auto" else "manual"}_updated", Date().time),
                    Updates.set("author.name", entry.author.name),
                    Updates.set("author.url", entry.author.url),
                    Updates.set("author.username", entry.author.username),
                    Updates.set("media", entry.media.map {
                        mapOf(
                            "index" to it.index,
                            "filename" to it.filename,
                            "original" to it.original,
                            "ext" to it.ext
                        )
                    }),
                    Updates.set("popularity.like", entry.popularity.like),
                    Updates.set("popularity.bookmark", entry.popularity.bookmark),
                    Updates.set("popularity.view", entry.popularity.view),
                    Updates.set("popularity.retweet", entry.popularity.retweet),
                    Updates.set("popularity.reply", entry.popularity.reply)
                )
            )

            if (!auto) {
                logger.info { "${entry.author.name} (${entry.platform}): \"${entry.title}\" (${entry.url}) を更新しました。" }
            }
        } else {
            collection.insertOne(Document.parse(jsonObjectOf(
                "title" to title,
                "description" to description,
                "url" to entry.url,
                "tags" to entry.tags.map {
                    mapOf(
                        "value" to (tagReplaceTable.findOne(Filters.eq("from", it))?.toTagReplaceTable()?.to ?: it),
                        "user" to null,
                        "locked" to true
                    )
                }.distinctBy { it["value"] },
                "user" to entry.user,

                "platform" to entry.platform,
                "sensitive_level" to entry.sensitiveLevel,

                "timestamp" to mapOf(
                    "created" to entry.created,
                    "added" to Calendar.getInstance().timeInMillis,
                    "auto_updated" to Calendar.getInstance().timeInMillis,
                    "manual_updated" to Calendar.getInstance().timeInMillis
                ),
                "author" to mapOf(
                    "name" to entry.author.name,
                    "url" to entry.author.url,
                    "username" to entry.author.username
                ),
                "media" to entry.media.map {
                    mapOf(
                        "index" to it.index,
                        "filename" to it.filename,
                        "original" to it.original,
                        "ext" to it.ext
                    )
                },
                "rating" to mapOf(
                    "count" to 0,
                    "score" to 0
                ),
                "popularity" to mapOf(
                    "like" to entry.popularity.like,
                    "bookmark" to entry.popularity.bookmark,
                    "view" to entry.popularity.view,
                    "retweet" to entry.popularity.retweet,
                    "reply" to entry.popularity.reply
                )
            ).stringify()))

            logger.info { "${entry.author.name} (${entry.platform}): \"${entry.title}\" (${entry.url}) を追加しました。" }
        }
    }

    data class Entry(val title: String, val description: String, val url: String, val tags: List<String>, val user: String?, val platform: String, val sensitiveLevel: Int, val created: Long, val author: Author, val media: List<Picture>, val popularity: Popularity) {
        data class Author(val name: String, val url: String, val username: String?)
        data class Picture(val index: Int, val filename: String, val original: String, val ext: String)
        data class Popularity(val like: Int? = null, val bookmark: Int? = null, val view: Int? = null, val retweet: Int? = null, val reply: Int? = null)
    }
}
