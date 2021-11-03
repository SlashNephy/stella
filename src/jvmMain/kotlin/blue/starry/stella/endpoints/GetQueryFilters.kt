package blue.starry.stella.endpoints

import blue.starry.stella.models.FileExtension
import blue.starry.stella.models.PicModel
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
                PicModel::title.regex(value, "im")
            )
        }
    }

    fun description(value: String?) = flow {
        if (!value.isNullOrBlank()) {
            emit(
                PicModel::description.regex(value, "im")
            )
        }
    }

    fun tags(value: String?) = flow {
        if (!value.isNullOrBlank()) {
            emit(
                or(
                    value.split(",")
                        .map {
                            PicModel::tags.elemMatch(PicModel.Tag::value.regex(it.trim(), "i"))
                        }
                )
            )
        }
    }

    fun platform(value: String?) = flow {
        val platform = value?.let { PlatformSerializer.deserializeOrNull(it) }

        if (platform != null) {
            emit(
                Filters.eq(PicModel::platform.name, PlatformSerializer.serialize(platform))
            )
        }
    }

    fun author(value: String?) = flow {
        if (!value.isNullOrBlank()) {
            emit(
                or(
                    (PicModel::author / PicModel.Author::name).regex(value, "im"),
                    (PicModel::author / PicModel.Author::username).regex(value, "im")
                )
            )
        }
    }

    fun user(value: String?) = flow {
        if (!value.isNullOrBlank()) {
            emit(
                PicModel::user eq value
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
                    Filters.`in`(PicModel::sensitive_level.name, levels.map { SensitiveLevelSerializer.serialize(it) })
                )
            }
        } else {
            emit(
                Filters.eq(PicModel::sensitive_level.name, SensitiveLevelSerializer.serialize(PicModel.SensitiveLevel.Safe))
            )
        }
    }

    fun created(since: String?, until: String?) = flow<Bson> {
        if (since != null) {
            runCatching {
                LocalDate.parse(since)
            }.onSuccess { d ->
                emit(
                    PicModel::timestamp / PicModel.Timestamp::created gte d.toEpochDay()
                )
            }
        }

        if (until != null) {
            runCatching {
                LocalDate.parse(until)
            }.onSuccess { d ->
                emit(
                    PicModel::timestamp / PicModel.Timestamp::created lte d.toEpochDay()
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
                    PicModel::timestamp / PicModel.Timestamp::added gte d.toEpochDay()
                )
            }
        }

        if (until != null) {
            runCatching {
                LocalDate.parse(until)
            }.onSuccess { d ->
                emit(
                    PicModel::timestamp / PicModel.Timestamp::added lte d.toEpochDay()
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
                    PicModel::timestamp / PicModel.Timestamp::manual_updated gte d.toEpochDay()
                )
            }
        }

        if (until != null) {
            runCatching {
                LocalDate.parse(until)
            }.onSuccess { d ->
                emit(
                    PicModel::timestamp / PicModel.Timestamp::manual_updated lte d.toEpochDay()
                )
            }
        }
    }

    fun extenstion(value: String?) = flow {
        val extension = value?.toFileExtension()
        if (extension != null) {
            emit(
                or(extension.exts.map { PicModel::media.elemMatch(PicModel.Media::ext eq it) })
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
                not(PicModel::popularity / PicModel.Popularity::like eq null)
            )
            emit(
                PicModel::popularity / PicModel.Popularity::like gte min
            )
        }

        if (max != null) {
            emit(
                not(PicModel::popularity / PicModel.Popularity::like eq null)
            )
            emit(
                PicModel::popularity / PicModel.Popularity::like lte min
            )
        }
    }

    fun bookmark(min: Int?, max: Int?) = flow {
        if (min != null) {
            emit(
                not(PicModel::popularity / PicModel.Popularity::bookmark eq null)
            )
            emit(
                PicModel::popularity / PicModel.Popularity::bookmark gte min
            )
        }

        if (max != null) {
            emit(
                not(PicModel::popularity / PicModel.Popularity::bookmark eq null)
            )
            emit(
                PicModel::popularity / PicModel.Popularity::bookmark lte min
            )
        }
    }

    fun view(min: Int?, max: Int?) = flow {
        if (min != null) {
            emit(
                not(PicModel::popularity / PicModel.Popularity::view eq null)
            )
            emit(
                PicModel::popularity / PicModel.Popularity::view gte min
            )
        }

        if (max != null) {
            emit(
                not(PicModel::popularity / PicModel.Popularity::view eq null)
            )
            emit(
                PicModel::popularity / PicModel.Popularity::view lte min
            )
        }
    }

    fun retweet(min: Int?, max: Int?) = flow {
        if (min != null) {
            emit(
                not(PicModel::popularity / PicModel.Popularity::retweet eq null)
            )
            emit(
                PicModel::popularity / PicModel.Popularity::retweet gte min
            )
        }

        if (max != null) {
            emit(
                not(PicModel::popularity / PicModel.Popularity::retweet eq null)
            )
            emit(
                PicModel::popularity / PicModel.Popularity::retweet lte min
            )
        }
    }

    fun reply(min: Int?, max: Int?) = flow {
        if (min != null) {
            emit(
                not(PicModel::popularity / PicModel.Popularity::reply eq null)
            )
            emit(
                PicModel::popularity / PicModel.Popularity::reply gte min
            )
        }

        if (max != null) {
            emit(
                not(PicModel::popularity / PicModel.Popularity::reply eq null)
            )
            emit(
                PicModel::popularity / PicModel.Popularity::reply lte min
            )
        }
    }
}
