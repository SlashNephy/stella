package blue.starry.stella.register

import blue.starry.stella.Stella
import blue.starry.stella.models.PicTagReplace
import org.litote.kmongo.eq

object Normalizer {
    fun normalizeTitle(title: String): String {
        return title.replace("\r\n", " ")
            .replace("\n", " ")
            .replace("<br>", " ")
    }

    fun normalizeDescription(description: String): String {
        return description.replace("\r\n", "<br>")
            .replace("\n", "<br>")
    }

    suspend fun normalizeTag(tag: String): String {
        return Stella.TagReplaceCollection.findOne(PicTagReplace::from eq tag)?.to ?: tag
    }
}
