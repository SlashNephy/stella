package blue.starry.stella.db

import blue.starry.stella.models.PicEntry
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import org.litote.kmongo.*

object GetQueryTagsFilters {
    fun ensureNotEmpty() = flowOf(
        not(PicEntry::tags.size(0))
    )

    fun name(value: String?) = flow {
        if (value != null) {
            emit(
                PicEntry::tags.elemMatch(PicEntry.Tag::value.regex(value, "i"))
            )
        }
    }

    fun existingTags(tags: List<String>) = flow {
        if (tags.isNotEmpty()) {
            emit(
                or(tags.map { tag ->
                    PicEntry::tags.elemMatch(PicEntry.Tag::value.regex(tag, "i"))
                })
            )
        }
    }

    fun sensitiveLevel(value: PicEntry.SensitiveLevel?) = flow {
        if (value != null) {
            emit(
                PicEntry::sensitive_level.lte(value)
            )
        }
    }
}
