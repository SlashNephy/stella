package blue.starry.stella.platforms.pixiv.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Illust(
    val caption: String,
    @SerialName("create_date")
    val createDate: String,
    val height: Int,
    val id: Int,
    @SerialName("image_urls")
    val imageUrls: ImageUrls,
    @SerialName("is_bookmarked")
    val isBookmarked: Boolean,
    @SerialName("is_muted")
    val isMuted: Boolean,
    @SerialName("meta_pages")
    val metaPages: List<ImageUrls>,
    @SerialName("meta_single_page")
    val metaSinglePage: MetaSinglePage,
    @SerialName("page_count")
    val pageCount: Int,
    val restrict: Int,
    @SerialName("sanity_level")
    val sanityLevel: Int,
    // val series: Any?,
    val tags: List<Tag>,
    val title: String,
    val tools: List<String>,
    @SerialName("total_bookmarks")
    val totalBookmarks: Int,
    @SerialName("total_comments")
    val totalComments: Int,
    @SerialName("total_view")
    val totalView: Int,
    val type: String,
    val user: User,
    val visible: Boolean,
    val width: Int,
    @SerialName("x_restrict")
    val xRestrict: Int
) {
    @Serializable
    data class ImageUrls(
        val large: String,
        val medium: String,
        @SerialName("square_medium")
        val squareMedium: String
    )

    @Serializable
    data class MetaSinglePage(
        @SerialName("original_image_url")
        val originalImageUrl: String
    )

    @Serializable
    data class Tag(
        val name: String,
        @SerialName("translated_name")
        val translatedName: String?
    )

    @Serializable
    data class User(
        val account: String,
        val id: Int,
        @SerialName("is_followed")
        val isFollowed: Boolean,
        val name: String,
        @SerialName("profile_image_urls")
        val profileImageUrls: ProfileImageUrls
    ) {
        @Serializable
        data class ProfileImageUrls(
            val medium: String
        )
    }
}
