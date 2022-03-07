package blue.starry.stella.register

import blue.starry.stella.Stella
import blue.starry.stella.models.PicTagReplace
import org.litote.kmongo.eq

object Normalizer {
    fun normalizeTitle(title: String): String {
        return title.replace("(?:\\s|<br>)+".toRegex(), " ")
    }

    fun normalizeDescription(description: String): String {
        return description.replace("[\r\n]+".toRegex(), "<br>").replace("\\s+".toRegex(), " ")
    }

    suspend fun normalizeTag(tag: String): String {
        val filter = PicTagReplace::from eq tag
        return Stella.TagReplaceCollection.findOne(filter)?.to ?: tag
    }
}
