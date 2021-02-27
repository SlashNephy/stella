package blue.starry.stella.endpoints

import blue.starry.stella.models.PicModel
import blue.starry.stella.models.PicTagsModel
import blue.starry.stella.worker.StellaMongoDBPicCollection
import io.ktor.application.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.coroutines.flow.flattenConcat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId
import org.litote.kmongo.and
import org.litote.kmongo.coroutine.aggregate
import org.litote.kmongo.eq
import org.litote.kmongo.id.toId
import org.litote.kmongo.match
import org.litote.kmongo.toId

@Location("/query/tags")
data class GetQueryTags(
    val id: String? = null,
    val name: String? = null,
    val sensitive_level: Int? = null,
    val count: Int = 10,
    val random: Boolean = false
)

fun Route.getQueryTags() {
    get<GetQueryTags> { param ->
        val existingTags = param.id?.let { id ->
            StellaMongoDBPicCollection.findOne(PicModel::_id eq ObjectId(id).toId())?.tags
        }?.map {
            it.value
        }.orEmpty()

        val filters = flowOf(
            GetQueryTagsFilters.ensureNotEmpty(),
            GetQueryTagsFilters.name(param.name),
            GetQueryTagsFilters.existingTags(existingTags),
            GetQueryTagsFilters.sensitiveLevel(param.sensitive_level)
        ).flattenConcat().toList()

        val tags = StellaMongoDBPicCollection
            .aggregate<PicModel>(match(and(filters)))
            .toList()
            .asSequence()
            .flatMap { it.tags }
            .map { it.value }
            .distinct()
            .filter { it !in existingTags }
            .let {
                if (param.random) {
                    it.shuffled()
                } else {
                    it
                }
            }
            .take(param.count)
            .toList()

        call.respond(
            PicTagsModel(
                tags = tags
            )
        )
    }
}
