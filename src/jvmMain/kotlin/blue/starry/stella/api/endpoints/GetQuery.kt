package blue.starry.stella.api.endpoints

import blue.starry.stella.api.*
import blue.starry.stella.collection
import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import io.ktor.application.call
import io.ktor.routing.Route
import io.ktor.routing.get
import org.apache.commons.lang3.time.FastDateFormat
import org.bson.Document
import org.bson.conversions.Bson
import org.litote.kmongo.or
import java.util.*

private val dateFormat = FastDateFormat.getInstance("yyyy-MM-dd", Locale.ENGLISH)

fun Route.getQuery() {
    get("/query") {
        val page = call.parameters["page"]?.toIntOrNull() ?: 0
        val count = call.parameters["count"]?.toIntOrNull() ?: 25

        val filter = mutableListOf<Bson>().also { filters ->
            val title = call.parameters["title"]
            if (!title.isNullOrBlank()) {
                filters += Filters.regex("title", title, "im")
            }

            val description = call.parameters["description"]
            if (!description.isNullOrBlank()) {
                filters += Filters.regex("description", description, "im")
            }

            val tags = call.parameters["tags"]
            if (!tags.isNullOrBlank()) {
                filters += tags.split(",").map {
                    Filters.elemMatch("tags", Filters.regex("value", it.trim(), "i"))
                }.reduce { s1, s2 ->
                    or(s1, s2)
                }
            }

            val author = call.parameters["author"]
            val platform = call.parameters["platform"]?.toImagePlatform()
            if (!author.isNullOrBlank()) {
                filters += or(
                    Filters.regex("author.name", author, "im"), Filters.regex("author.username", author, "im")
                )
            }

            if (platform != null) {
                filters += Filters.eq("platform", platform.name)
            }

            val user = call.parameters["user"]
            if (!user.isNullOrBlank()) {
                filters += Filters.eq("user", user)
            }

            val levels = call.parameters["sensitive_levels"].orEmpty().split(",").mapNotNull { it.toIntOrNull() }
            if (levels.isNotEmpty()) {
                filters += Filters.`in`("sensitive_level", levels)
            }

            val createdSince = call.parameters["created_since"]
            if (createdSince != null) {
                runCatching {
                    dateFormat.parse(createdSince)
                }.onSuccess { d ->
                    filters += Filters.gte("timestamp.created", d.time)
                }
            }
            val createdUntil = call.parameters["created_until"]
            if (createdUntil != null) {
                runCatching {
                    dateFormat.parse(createdUntil)
                }.onSuccess { d ->
                    filters += Filters.lte("timestamp.created", d.time)
                }
            }

            val addedSince = call.parameters["added_since"]
            if (addedSince != null) {
                runCatching {
                    dateFormat.parse(addedSince)
                }.onSuccess { d ->
                    filters += Filters.gte("timestamp.added", d.time)
                }
            }
            val addedUntil = call.parameters["added_until"]
            if (addedUntil != null) {
                runCatching {
                    dateFormat.parse(addedUntil)
                }.onSuccess { d ->
                    filters += Filters.lte("timestamp.added", d.time)
                }
            }

            val updatedSince = call.parameters["updated_since"]
            if (updatedSince != null) {
                runCatching {
                    dateFormat.parse(updatedSince)
                }.onSuccess { d ->
                    filters += Filters.gte("timestamp.manual_updated", d.time)
                }
            }
            val updatedUntil = call.parameters["updated_until"]
            if (updatedUntil != null) {
                runCatching {
                    dateFormat.parse(updatedUntil)
                }.onSuccess { d ->
                    filters += Filters.lte("timestamp.manual_updated", d.time)
                }
            }

            val extension = call.parameters["ext"]?.toFileExtension()
            if (extension != null) {
                filters += or(extension.exts.map { Filters.elemMatch("media", Filters.eq("ext", it)) })
            }

            val minRating = call.parameters["min_rating"]?.toIntOrNull()
            if (minRating != null) {
                filters += Filters.expr("{\$gte: [\$divide: [\"\$rating.score\", \"\$rating.count\"], $minRating]}")
            }
            val maxRating = call.parameters["max_rating"]?.toIntOrNull()
            if (maxRating != null) {
                filters += Filters.expr("{\$lte: [\$divide: [\"\$rating.score\", \"\$rating.count\"], $maxRating]}")
            }

            val minLike = call.parameters["min_like"]?.toIntOrNull()
            val maxLike = call.parameters["max_like"]?.toIntOrNull()
            val minBookmark = call.parameters["min_bookmark"]?.toIntOrNull()
            val maxBookmark = call.parameters["max_bookmark"]?.toIntOrNull()
            val minView = call.parameters["min_view"]?.toIntOrNull()
            val maxView = call.parameters["max_view"]?.toIntOrNull()
            val minRetweet = call.parameters["min_retweet"]?.toIntOrNull()
            val maxRetweet = call.parameters["max_retweet"]?.toIntOrNull()
            val minReply = call.parameters["min_reply"]?.toIntOrNull()
            val maxReply = call.parameters["max_reply"]?.toIntOrNull()

            if (minLike != null) {
                filters += Filters.not(Filters.eq("popularity.like", null))
                filters += Filters.gte("popularity.like", minLike)
            }
            if (maxLike != null) {
                filters += Filters.not(Filters.eq("popularity.like", null))
                filters += Filters.lte("popularity.like", maxLike)
            }
            if (minBookmark != null) {
                filters += Filters.not(Filters.eq("popularity.bookmark", null))
                filters += Filters.gte("popularity.bookmark", minBookmark)
            }
            if (maxBookmark != null) {
                filters += Filters.not(Filters.eq("popularity.bookmark", null))
                filters += Filters.lte("popularity.bookmark", maxBookmark)
            }
            if (minView != null) {
                filters += Filters.not(Filters.eq("popularity.view", null))
                filters += Filters.gte("popularity.view", minView)
            }
            if (maxView != null) {
                filters += Filters.not(Filters.eq("popularity.view", null))
                filters += Filters.lte("popularity.view", maxView)
            }
            if (minRetweet != null) {
                filters += Filters.not(Filters.eq("popularity.retweet", null))
                filters += Filters.gte("popularity.retweet", minRetweet)
            }
            if (maxRetweet != null) {
                filters += Filters.not(Filters.eq("popularity.retweet", null))
                filters += Filters.lte("popularity.retweet", maxRetweet)
            }
            if (minReply != null) {
                filters += Filters.not(Filters.eq("popularity.reply", null))
                filters += Filters.gte("popularity.reply", minReply)
            }
            if (maxReply != null) {
                filters += Filters.not(Filters.eq("popularity.reply", null))
                filters += Filters.lte("popularity.reply", maxReply)
            }
        }.let { filters ->
            if (filters.isNotEmpty()) {
                Filters.and(filters)
            } else {
                null
            }
        }

        val pipeline = mutableListOf<Bson>().also { pipelines ->
            val order = call.parameters["sort"]?.toSortOrder() ?: SortOrder.ManualUpdatedDescending
            when (order) {
                SortOrder.AddedDescending -> {
                    pipelines += Aggregates.sort(Sorts.descending("timestamp.added"))
                }
                SortOrder.AddedAscending -> {
                    pipelines += Aggregates.sort(Sorts.ascending("timestamp.added"))
                }
                SortOrder.CreatedDescending -> {
                    pipelines += Aggregates.sort(Sorts.descending("timestamp.created"))
                }
                SortOrder.CreatedAscending -> {
                    pipelines += Aggregates.sort(Sorts.ascending("timestamp.created"))
                }
                SortOrder.ManualUpdatedDescending -> {
                    pipelines += Aggregates.sort(Sorts.descending("timestamp.manual_updated"))
                }
                SortOrder.ManualUpdatedAscending -> {
                    pipelines += Aggregates.sort(Sorts.ascending("timestamp.manual_updated"))
                }
                SortOrder.AutoUpdatedDescending -> {
                    pipelines += Aggregates.sort(Sorts.descending("timestamp.auto_updated"))
                }
                SortOrder.AutoUpdatedAscending -> {
                    pipelines += Aggregates.sort(Sorts.ascending("timestamp.auto_updated"))
                }
                SortOrder.TitleDescending -> {
                    pipelines += Aggregates.sort(Sorts.descending("title"))
                }
                SortOrder.TitleAscending -> {
                    pipelines += Aggregates.sort(Sorts.ascending("title"))
                }
                SortOrder.AuthorDescending -> {
                    pipelines += Aggregates.sort(Sorts.descending("author.name"))
                }
                SortOrder.AuthorAscending -> {
                    pipelines += Aggregates.sort(Sorts.ascending("author.name"))
                }
                SortOrder.RatingDescending -> {
                    pipelines += Aggregates.sort(Sorts.descending("rating.score"))
                }
                SortOrder.RatingAscending -> {
                    pipelines += Aggregates.sort(Sorts.ascending("rating.score"))
                }
                SortOrder.LikeDescending -> {
                    pipelines += Aggregates.match(Filters.not(Filters.eq("popularity.like", null)))
                    pipelines += Aggregates.sort(Sorts.descending("popularity.like"))
                }
                SortOrder.LikeAscending -> {
                    pipelines += Aggregates.match(Filters.not(Filters.eq("popularity.like", null)))
                    pipelines += Aggregates.sort(Sorts.ascending("popularity.like"))
                }
                SortOrder.BookmarkDescending -> {
                    pipelines += Aggregates.match(Filters.not(Filters.eq("popularity.bookmark", null)))
                    pipelines += Aggregates.sort(Sorts.descending("popularity.bookmark"))
                }
                SortOrder.BookmarkAscending -> {
                    pipelines += Aggregates.match(Filters.not(Filters.eq("popularity.bookmark", null)))
                    pipelines += Aggregates.sort(Sorts.ascending("popularity.bookmark"))
                }
                SortOrder.ViewDescending -> {
                    pipelines += Aggregates.match(Filters.not(Filters.eq("popularity.view", null)))
                    pipelines += Aggregates.sort(Sorts.descending("popularity.view"))
                }
                SortOrder.ViewAscending -> {
                    pipelines += Aggregates.match(Filters.not(Filters.eq("popularity.view", null)))
                    pipelines += Aggregates.sort(Sorts.ascending("popularity.view"))
                }
                SortOrder.RetweetDescending -> {
                    pipelines += Aggregates.match(Filters.not(Filters.eq("popularity.retweet", null)))
                    pipelines += Aggregates.sort(Sorts.descending("popularity.retweet"))
                }
                SortOrder.RetweetAscending -> {
                    pipelines += Aggregates.match(Filters.not(Filters.eq("popularity.retweet", null)))
                    pipelines += Aggregates.sort(Sorts.ascending("popularity.retweet"))
                }
                SortOrder.ReplyDescending -> {
                    pipelines += Aggregates.match(Filters.not(Filters.eq("popularity.reply", null)))
                    pipelines += Aggregates.sort(Sorts.descending("popularity.reply"))
                }
                SortOrder.ReplyAscending -> {
                    pipelines += Aggregates.match(Filters.not(Filters.eq("popularity.reply", null)))
                    pipelines += Aggregates.sort(Sorts.ascending("popularity.reply"))
                }
                SortOrder.Random -> {
                    pipelines += Aggregates.sample(count)
                }
            }

            if (filter != null) {
                pipelines += Aggregates.match(filter)
            }

            if (order != SortOrder.Random) {
                pipelines += Aggregates.skip(page * count)
                pipelines += Aggregates.limit(count)
            }
        }


        call.respondApi {
            collection.aggregate<Document>(pipeline).toList().serialize()
        }
    }
}
