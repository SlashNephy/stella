package blue.starry.stella.endpoints

import blue.starry.stella.models.PicModel
import blue.starry.stella.models.PicTagsModel
import blue.starry.stella.worker.StellaMongoDBPicCollection
import io.ktor.application.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*
import org.litote.kmongo.*
import org.litote.kmongo.coroutine.aggregate

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
            StellaMongoDBPicCollection.findOne(PicModel::_id eq id.toId())?.tags
        }.orEmpty().map {
            it.value
        }

        val filters = buildList {
            this += not(PicModel::tags.size(0))

            if (param.name != null) {
                this += PicModel::tags.elemMatch(PicModel.Tag::value.regex(param.name, "i"))
            }

            if (existingTags.isNotEmpty()) {
                this += or(existingTags.map { tag ->
                    PicModel::tags.elemMatch(PicModel.Tag::value.regex(tag, "i"))
                })
            }

            if (param.sensitive_level != null) {
                this += PicModel::sensitive_level lte param.sensitive_level
            }
        }

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
