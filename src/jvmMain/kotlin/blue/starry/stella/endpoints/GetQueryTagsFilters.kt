package blue.starry.stella.endpoints

import blue.starry.stella.models.PicModel
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import org.litote.kmongo.*

object GetQueryTagsFilters {
    fun ensureNotEmpty() = flowOf(
        not(PicModel::tags.size(0))
    )

    fun name(value: String?) = flow {
        if (value != null) {
            emit(
                PicModel::tags.elemMatch(PicModel.Tag::value.regex(value, "i"))
            )
        }
    }

    fun existingTags(tags: List<String>) = flow {
        if (tags.isNotEmpty()) {
            emit(
                or(tags.map { tag ->
                    PicModel::tags.elemMatch(PicModel.Tag::value.regex(tag, "i"))
                })
            )
        }
    }

    fun sensitiveLevel(value: Int?) = flow {
        if (value != null) {
            emit(
                PicModel::sensitive_level lte value
            )
        }
    }
}
