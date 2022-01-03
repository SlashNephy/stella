package blue.starry.stella.db

import blue.starry.stella.models.PicEntry
import blue.starry.stella.models.internal.PlatformSerializer
import blue.starry.stella.models.internal.SensitiveLevelSerializer
import kotlinx.coroutines.flow.flow
import org.litote.kmongo.*
import java.time.LocalDate
import kotlin.reflect.KProperty1

object GetQueryFilters {
    fun title(value: String?) = flow {
        if (!value.isNullOrBlank()) {
            emit(
                PicEntry::title.regex(value.trim(), "im")
            )
        }
    }

    fun description(value: String?) = flow {
        if (!value.isNullOrBlank()) {
            emit(
                PicEntry::description.regex(value.trim(), "im")
            )
        }
    }

    fun tags(value: String?) = flow {
        if (!value.isNullOrBlank()) {
            emit(
                or(
                    value.split(",").map { tag ->
                        PicEntry::tags.elemMatch(PicEntry.Tag::value.regex(tag.trim(), "i"))
                    }
                )
            )
        }
    }

    fun platform(value: String?) = flow {
        val platform = value?.let { PlatformSerializer.deserializeOrNull(it.trim()) }
        if (platform != null) {
            emit(
                PicEntry::platform.eq(platform)
            )
        }
    }

    fun author(value: String?) = flow {
        if (!value.isNullOrBlank()) {
            emit(
                or(
                    (PicEntry::author / PicEntry.Author::name).regex(value.trim(), "im"),
                    (PicEntry::author / PicEntry.Author::username).regex(value.trim(), "im")
                )
            )
        }
    }

    fun sensitiveLevel(value: String?, trusted: Boolean) = flow {
        if (trusted) {
            val levels = value?.split(",")
                ?.mapNotNull { it.trim().toIntOrNull() }
                ?.mapNotNull { SensitiveLevelSerializer.deserializeOrNull(it) }

            if (!levels.isNullOrEmpty()) {
                emit(
                    PicEntry::sensitive_level.`in`(levels)
                )
            }
        } else {
            emit(
                PicEntry::sensitive_level.eq(PicEntry.SensitiveLevel.Safe)
            )
        }
    }

    fun created(since: String?, until: String?) = timestamp(PicEntry.Timestamp::created, since, until)
    fun added(since: String?, until: String?) = timestamp(PicEntry.Timestamp::added, since, until)
    fun updated(since: String?, until: String?) = timestamp(PicEntry.Timestamp::manual_updated, since, until)
    private fun timestamp(property: KProperty1<PicEntry.Timestamp, Any?>, since: String?, until: String?) = flow {
        if (since != null) {
            runCatching {
                LocalDate.parse(since.trim())
            }.onSuccess { date ->
                emit(
                    (PicEntry::timestamp / property).gte(date.toEpochDay())
                )
            }
        }

        if (until != null) {
            runCatching {
                LocalDate.parse(until.trim())
            }.onSuccess { date ->
                emit(
                    (PicEntry::timestamp / property).lte(date.toEpochDay())
                )
            }
        }
    }

    fun extenstion(value: String?) = flow {
        val extensions = value?.toMediaExtensions()?.distinct()
        if (!extensions.isNullOrEmpty()) {
            emit(
                (PicEntry::media / PicEntry.Media::ext).`in`(extensions)
            )
        }
    }

    private fun String.toMediaExtensions(): List<PicEntry.MediaExtension> {
        return when {
            equals("image", true) -> listOf(PicEntry.MediaExtension.jpeg, PicEntry.MediaExtension.jpg, PicEntry.MediaExtension.png)
            equals("video", true) -> listOf(PicEntry.MediaExtension.gif, PicEntry.MediaExtension.mp4)
            else -> PicEntry.MediaExtension.values().filter { it.name.equals(this, true) }
        }
    }

    fun rating(min: Int?, max: Int?) = flow {
        if (min != null) {
            emit(
                expr("{\$gte: [\$divide: [\"\$rating.score\", \"\$rating.count\"], $min]}")
            )
        }

        if (max != null) {
            emit(
                expr("{\$lte: [\$divide: [\"\$rating.score\", \"\$rating.count\"], $max]}")
            )
        }
    }

    fun like(min: Int?, max: Int?) = popularity(PicEntry.Popularity::like, min, max)
    fun bookmark(min: Int?, max: Int?) = popularity(PicEntry.Popularity::bookmark, min, max)
    fun view(min: Int?, max: Int?) = popularity(PicEntry.Popularity::view, min, max)
    fun retweet(min: Int?, max: Int?) = popularity(PicEntry.Popularity::retweet, min, max)
    fun reply(min: Int?, max: Int?) = popularity(PicEntry.Popularity::reply, min, max)
    private fun popularity(property: KProperty1<PicEntry.Popularity, Any?>, min: Int?, max: Int?) = flow {
        if (min != null) {
            emit(
                (PicEntry::popularity / property).ne(null)
            )
            emit(
                (PicEntry::popularity / property).gte(min)
            )
        }

        if (max != null) {
            emit(
                (PicEntry::popularity / property).ne(null)
            )
            emit(
                (PicEntry::popularity / property).lte(max)
            )
        }
    }
}
