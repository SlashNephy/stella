package blue.starry.stella.worker.platform

import blue.starry.jsonkt.JsonObject
import blue.starry.jsonkt.delegation.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

object PixivModel {
    data class Token(override val json: JsonObject): JsonModel {
        val response by model { Response(it) }

        data class Response(override val json: JsonObject): JsonModel {
            val user by model { User(it) }
            val tokenType by string("token_type")
            val scope by string
            val refreshToken by string("refresh_token")
            val deviceToken by string("device_token")
            val accessToken by string("access_token")
            val expiresIn by int("expires_in")

            data class User(override val json: JsonObject): JsonModel {
                val id by string
            //    "user" : {
            //      "profile_image_urls" : {
            //        "px_50x50" : "https://s.pximg.net/common/images/no_profile_s.png",
            //        "px_170x170" : "https://s.pximg.net/common/images/no_profile.png",
            //        "px_16x16" : "https://s.pximg.net/common/images/no_profile_ss.png"
            //      },
            //      "account" : "slashnephy",
            //      "mail_address" : "spica@starry.blue",
            //      "id" : "3009751",
            //      "is_premium" : false,
            //      "x_restrict" : 2,
            //      "require_policy_agreement" : false,
            //      "name" : "Nep",
            //      "is_mail_authorized" : true
            //    },
            }
        }
    }

    data class Bookmarks(override val json: JsonObject): JsonModel {
        val illusts by modelList { Bookmark(it) }
        val nextUrl by nullableString("next_url")

        data class Bookmark(override val json: JsonObject): JsonModel {
            val xRestrict by int("x_restrict")
            val totalView by int("total_view")
            val title by string
            val pageCount by int("page_count")
            val tags by modelList { Tag(it) }
            val restrict by int
            val tools by stringList
            val imageUrls by model("image_urls") { ImageUrls(it) }
            val metaSinglePage by model("meta_single_page") { MetaSinglePage(it) }
            val createDate by string("create_date")
            val type by string
            val caption by string
            val id by int
            val user by model { User(it) }
            val metaPages by modelList("meta_pages") { MetaPage(it) }
            val height by int
            val totalBookmarks by int("total_bookmarks")
            val width by int
            val isBookmarked by boolean("is_bookmarked")
            val sanityLevel by int("sanity_level")
            val isMuted by boolean("is_muted")
            val series by nullableJsonElement
            val visible by boolean

            data class Tag(override val json: JsonObject): JsonModel {
                val name by string
                val translatedName by nullableString("translated_name")
            }

            data class ImageUrls(override val json: JsonObject): JsonModel {
                val large by string
                val squareMedium by string("square_medium")
                val medium by string
                val original by nullableString
            }

            data class MetaSinglePage(override val json: JsonObject): JsonModel {
                val originalImageUrl by nullableString("original_image_url")
            }

            data class MetaPage(override val json: JsonObject): JsonModel {
                val imageUrls by model("image_urls") { ImageUrls(it) }
            }

            data class User(override val json: JsonObject): JsonModel {
                val id by int
                val account by string
                val name by string
                val profileImageUrls by model("profile_image_urls") { ProfileImageUrls(it) }
                val isFollowed by boolean("is_followed")

                data class ProfileImageUrls(override val json: JsonObject): JsonModel {
                    val medium by string
                }
            }
        }
    }

    @Serializable
    data class AjaxResponse<T>(
        val body: T,
        val error: Boolean,
        val message: String
    )

    @Serializable
    data class Illust(
        val alt: String,
        // val bookStyle: Int,
        val bookmarkCount: Int,
        // val bookmarkData: Any?,
        // val comicPromotion: Any?,
        val commentCount: Int,
        val commentOff: Int,
        // val contestBanners: List<Any>,
        // val contestData: Any?,
        val createDate: String,
        val description: String,
        // val descriptionBoothId: Any?,
        // val descriptionYoutubeId: Any?,
        val extraData: ExtraData,
        val fanboxPromotion: FanboxPromotion,
        val height: Int,
        val id: String,
        val illustComment: String,
        val illustId: String,
        val illustTitle: String,
        val illustType: Int,
        val imageResponseCount: Int,
        // val imageResponseData: List<Any>,
        // val imageResponseOutData: List<Any>,
        val isBookmarkable: Boolean,
        val isHowto: Boolean,
        val isOriginal: Boolean,
        val isUnlisted: Boolean,
        val likeCount: Int,
        val likeData: Boolean,
        val pageCount: Int,
        // val pollData: Any?,
        // val request: Any?,
        val responseCount: Int,
        val restrict: Int,
        // val seriesNavData: Any?,
        val sl: Int,
        val storableTags: List<String>,
        val tags: Tags,
        val title: String,
        val titleCaptionTranslation: TitleCaptionTranslation,
        val uploadDate: String,
        val urls: Urls,
        val userAccount: String,
        val userId: String,
        val userIllusts: Map<String, UserIllust?>,
        val userName: String,
        val viewCount: Int,
        val width: Int,
        val xRestrict: Int,
        val zoneConfig: Map<String, ZoneConfig>
    ) {
        @Serializable
        data class ExtraData(
            val meta: Meta
        ) {
            @Serializable
            data class Meta(
                val alternateLanguages: AlternateLanguages,
                val canonical: String,
                val description: String,
                val descriptionHeader: String,
                val ogp: Ogp,
                val title: String,
                val twitter: Twitter
            ) {
                @Serializable
                data class AlternateLanguages(
                    val en: String,
                    val ja: String
                )

                @Serializable
                data class Ogp(
                    val description: String,
                    val image: String,
                    val title: String,
                    val type: String
                )

                @Serializable
                data class Twitter(
                    val card: String,
                    val description: String,
                    val image: String,
                    val title: String
                )
            }
        }

        @Serializable
        data class FanboxPromotion(
            val userName: String,
            val userImageUrl: String,
            val contentUrl: String,
            val description: String,
            val imageUrl: String,
            val imageUrlMobile: String,
            val hasAdultContent: Boolean
        )

        @Serializable
        data class Tags(
            val authorId: String,
            val isLocked: Boolean,
            val tags: List<Tag>,
            val writable: Boolean
        ) {
            @Serializable
            data class Tag(
                val deletable: Boolean,
                val locked: Boolean,
                val tag: String,
                val userId: String? = null,
                val userName: String? = null
            )
        }

        @Serializable
        data class TitleCaptionTranslation(
            val workCaption: String?,
            val workTitle: String?
        )

        @Serializable
        data class Urls(
            val mini: String,
            val original: String,
            val regular: String,
            val small: String,
            val thumb: String
        )

        @Serializable
        data class UserIllust(
            val alt: String,
            // val bookmarkData: Any?,
            val createDate: String,
            val description: String,
            val height: Int,
            val id: String,
            val illustType: Int,
            val isBookmarkable: Boolean,
            val isMasked: Boolean,
            val isUnlisted: Boolean,
            val pageCount: Int,
            val profileImageUrl: String? = null,
            val restrict: Int,
            val sl: Int,
            val tags: List<String>,
            val title: String,
            val titleCaptionTranslation: TitleCaptionTranslation,
            val updateDate: String,
            val url: String,
            val userId: String,
            val userName: String,
            val width: Int,
            val xRestrict: Int
        )

        @Serializable
        data class ZoneConfig(
            val url: String
        )
    }

    @Serializable
    data class IllustPage(
        val height: Int,
        val urls: Urls,
        val width: Int
    ) {
        @Serializable
        data class Urls(
            val original: String,
            val regular: String,
            val small: String,
            @SerialName("thumb_mini")
            val thumbMini: String
        )
    }

    @Serializable
    data class IllustUgoiraMeta(
        val frames: List<Frame>,
        @SerialName("mime_type")
        val mimeType: String,
        val originalSrc: String,
        val src: String
    ) {
        @Serializable
        data class Frame(
            val delay: Int,
            val file: String
        )
    }
}
