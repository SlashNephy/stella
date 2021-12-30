package blue.starry.stella.server

import blue.starry.stella.models.FileExtension
import blue.starry.stella.models.PicEntry
import blue.starry.stella.models.internal.PlatformSerializer
import blue.starry.stella.models.internal.SensitiveLevelSerializer
import com.mongodb.client.model.Filters
import kotlinx.coroutines.flow.flow
import org.bson.conversions.Bson
import org.litote.kmongo.*
import java.time.LocalDate

object GetQueryFilters {
    fun title(value: String?) = flow {
        if (!value.isNullOrBlank()) {
            emit(
                PicEntry::title.regex(value, "im")
            )
        }
    }

    fun description(value: String?) = flow {
        if (!value.isNullOrBlank()) {
            emit(
                PicEntry::description.regex(value, "im")
            )
        }
    }

    fun tags(value: String?) = flow {
        if (!value.isNullOrBlank()) {
            emit(
                or(
                    value.split(",")
                        .map {
                            PicEntry::tags.elemMatch(PicEntry.Tag::value.regex(it.trim(), "i"))
                        }
                )
            )
        }
    }

    fun platform(value: String?) = flow {
        val platform = value?.let { PlatformSerializer.deserializeOrNull(it) }

        if (platform != null) {
            emit(
                Filters.eq(PicEntry::platform.name, PlatformSerializer.serialize(platform))
            )
        }
    }

    fun author(value: String?) = flow {
        if (!value.isNullOrBlank()) {
            emit(
                or(
                    (PicEntry::author / PicEntry.Author::name).regex(value, "im"),
                    (PicEntry::author / PicEntry.Author::username).regex(value, "im")
                )
            )
        }
    }

    fun user(value: String?) = flow {
        if (!value.isNullOrBlank()) {
            emit(
                PicEntry::user eq value
            )
        }
    }

    fun sensitiveLevel(value: String?, trusted: Boolean) = flow {
        if (trusted) {
            val levels = value?.split(",")
                ?.mapNotNull { it.toIntOrNull() }
                ?.mapNotNull { SensitiveLevelSerializer.deserializeOrNull(it) }
                .orEmpty()

            if (levels.isNotEmpty()) {
                emit(
                    Filters.`in`(PicEntry::sensitive_level.name, levels.map { SensitiveLevelSerializer.serialize(it) })
                )
            }
        } else {
            emit(
                Filters.eq(PicEntry::sensitive_level.name, SensitiveLevelSerializer.serialize(PicEntry.SensitiveLevel.Safe))
            )
        }
    }

    fun created(since: String?, until: String?) = flow<Bson> {
        if (since != null) {
            runCatching {
                LocalDate.parse(since)
            }.onSuccess { d ->
                emit(
                    PicEntry::timestamp / PicEntry.Timestamp::created gte d.toEpochDay()
                )
            }
        }

        if (until != null) {
            runCatching {
                LocalDate.parse(until)
            }.onSuccess { d ->
                emit(
                    PicEntry::timestamp / PicEntry.Timestamp::created lte d.toEpochDay()
                )
            }
        }
    }

    fun added(since: String?, until: String?) = flow<Bson> {
        if (since != null) {
            runCatching {
                LocalDate.parse(since)
            }.onSuccess { d ->
                emit(
                    PicEntry::timestamp / PicEntry.Timestamp::added gte d.toEpochDay()
                )
            }
        }

        if (until != null) {
            runCatching {
                LocalDate.parse(until)
            }.onSuccess { d ->
                emit(
                    PicEntry::timestamp / PicEntry.Timestamp::added lte d.toEpochDay()
                )
            }
        }
    }

    fun updated(since: String?, until: String?) = flow<Bson> {
        if (since != null) {
            runCatching {
                LocalDate.parse(since)
            }.onSuccess { d ->
                emit(
                    PicEntry::timestamp / PicEntry.Timestamp::manual_updated gte d.toEpochDay()
                )
            }
        }

        if (until != null) {
            runCatching {
                LocalDate.parse(until)
            }.onSuccess { d ->
                emit(
                    PicEntry::timestamp / PicEntry.Timestamp::manual_updated lte d.toEpochDay()
                )
            }
        }
    }

    fun extenstion(value: String?) = flow {
        val extension = value?.toFileExtension()
        if (extension != null) {
            emit(
                or(extension.exts.map { PicEntry::media.elemMatch(PicEntry.Media::ext eq it) })
            )
        }
    }

    private fun String.toFileExtension(): FileExtension? {
        return FileExtension.values().find { equals(it.text, true) }
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

    fun like(min: Int?, max: Int?) = flow {
        if (min != null) {
            emit(
                not(PicEntry::popularity / PicEntry.Popularity::like eq null)
            )
            emit(
                PicEntry::popularity / PicEntry.Popularity::like gte min
            )
        }

        if (max != null) {
            emit(
                not(PicEntry::popularity / PicEntry.Popularity::like eq null)
            )
            emit(
                PicEntry::popularity / PicEntry.Popularity::like lte min
            )
        }
    }

    fun bookmark(min: Int?, max: Int?) = flow {
        if (min != null) {
            emit(
                not(PicEntry::popularity / PicEntry.Popularity::bookmark eq null)
            )
            emit(
                PicEntry::popularity / PicEntry.Popularity::bookmark gte min
            )
        }

        if (max != null) {
            emit(
                not(PicEntry::popularity / PicEntry.Popularity::bookmark eq null)
            )
            emit(
                PicEntry::popularity / PicEntry.Popularity::bookmark lte min
            )
        }
    }

    fun view(min: Int?, max: Int?) = flow {
        if (min != null) {
            emit(
                not(PicEntry::popularity / PicEntry.Popularity::view eq null)
            )
            emit(
                PicEntry::popularity / PicEntry.Popularity::view gte min
            )
        }

        if (max != null) {
            emit(
                not(PicEntry::popularity / PicEntry.Popularity::view eq null)
            )
            emit(
                PicEntry::popularity / PicEntry.Popularity::view lte min
            )
        }
    }

    fun retweet(min: Int?, max: Int?) = flow {
        if (min != null) {
            emit(
                not(PicEntry::popularity / PicEntry.Popularity::retweet eq null)
            )
            emit(
                PicEntry::popularity / PicEntry.Popularity::retweet gte min
            )
        }

        if (max != null) {
            emit(
                not(PicEntry::popularity / PicEntry.Popularity::retweet eq null)
            )
            emit(
                PicEntry::popularity / PicEntry.Popularity::retweet lte min
            )
        }
    }

    fun reply(min: Int?, max: Int?) = flow {
        if (min != null) {
            emit(
                not(PicEntry::popularity / PicEntry.Popularity::reply eq null)
            )
            emit(
                PicEntry::popularity / PicEntry.Popularity::reply gte min
            )
        }

        if (max != null) {
            emit(
                not(PicEntry::popularity / PicEntry.Popularity::reply eq null)
            )
            emit(
                PicEntry::popularity / PicEntry.Popularity::reply lte min
            )
        }
    }
}
