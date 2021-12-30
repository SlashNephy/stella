package blue.starry.stella.platforms.nijie.entities


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Illust(
    val author: Author,
    val contentLocation: String,
    @SerialName("@context")
    val context: String,
    val copyrightHolder: CopyrightHolder,
    val copyrightYear: String,
    val creator: Creator,
    val dateModified: String,
    val datePublished: String,
    val description: String,
    val editor: Editor,
    val genre: String,
    val height: Int,
    val interactionCount: String,
    val name: String,
    val text: String,
    val thumbnailUrl: String,
    @SerialName("@type")
    val type: String,
    val uploadDate: String,
    val width: Int
) {
    @Serializable
    data class Author(
        val description: String,
        val image: String,
        val name: String,
        val sameAs: String,
        @SerialName("@type")
        val type: String
    )

    @Serializable
    data class CopyrightHolder(
        val description: String,
        val image: String,
        val name: String,
        val sameAs: String,
        @SerialName("@type")
        val type: String
    )

    @Serializable
    data class Creator(
        val description: String,
        val image: String,
        val name: String,
        val sameAs: String,
        @SerialName("@type")
        val type: String
    )

    @Serializable
    data class Editor(
        val description: String,
        val image: String,
        val name: String,
        val sameAs: String,
        @SerialName("@type")
        val type: String
    )
}
