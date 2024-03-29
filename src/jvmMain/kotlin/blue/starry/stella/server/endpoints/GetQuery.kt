package blue.starry.stella.server.endpoints

import blue.starry.stella.Stella
import blue.starry.stella.db.GetQueryFilters
import blue.starry.stella.db.GetQueryPipelines
import blue.starry.stella.models.PicEntry
import io.ktor.application.call
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.request.header
import io.ktor.response.respond
import io.ktor.routing.Route
import kotlinx.coroutines.flow.flattenConcat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList

@Location("/query")
data class GetQuery(
    val title: String? = null,
    val description: String? = null,
    val tags: String? = null,
    val author: String? = null,
    val platform: String? = null,
    val user: String? = null,
    val sensitive_levels: String? = null,
    val kinds: String? = null,
    val created_since: String? = null,
    val created_until: String? = null,
    val added_since: String? = null,
    val added_until: String? = null,
    val updated_since: String? = null,
    val updated_until: String? = null,
    val ext: String? = null,
    val min_rating: Int? = null,
    val max_rating: Int? = null,
    val min_like: Int? = null,
    val max_like: Int? = null,
    val min_bookmark: Int? = null,
    val max_bookmark: Int? = null,
    val min_view: Int? = null,
    val max_view: Int? = null,
    val min_retweet: Int? = null,
    val max_retweet: Int? = null,
    val min_reply: Int? = null,
    val max_reply: Int? = null,
    val sort: String? = null,
    val page: Int = 0,
    val count: Int = 50
)

fun Route.getQuery() {
    get<GetQuery> { param ->
        val filters = flowOf(
            GetQueryFilters.title(param.title),
            GetQueryFilters.description(param.description),
            GetQueryFilters.tags(param.tags),
            GetQueryFilters.platform(param.platform),
            GetQueryFilters.author(param.author),
            GetQueryFilters.sensitiveLevel(param.sensitive_levels, call.request.header("X-Local-Access") == "1"),
            GetQueryFilters.kind(param.kinds),
            GetQueryFilters.created(param.created_since, param.created_until),
            GetQueryFilters.added(param.added_since, param.added_until),
            GetQueryFilters.updated(param.updated_since, param.updated_until),
            GetQueryFilters.extenstion(param.ext),
            GetQueryFilters.rating(param.min_rating, param.max_rating),
            GetQueryFilters.like(param.min_like, param.max_like),
            GetQueryFilters.bookmark(param.min_bookmark, param.max_bookmark),
            GetQueryFilters.view(param.min_view, param.max_view),
            GetQueryFilters.retweet(param.min_retweet, param.max_retweet),
            GetQueryFilters.reply(param.min_reply, param.max_reply)
        ).flattenConcat().toList()

        val pipeline = flowOf(
            GetQueryPipelines.sort(param.sort, param.count),
            GetQueryPipelines.applyFilters(filters),
            GetQueryPipelines.limit(param.sort, param.page, param.count)
        ).flattenConcat().toList()

        call.respond(
            Stella.PicCollection.aggregate<PicEntry>(pipeline).toList()
        )
    }
}
