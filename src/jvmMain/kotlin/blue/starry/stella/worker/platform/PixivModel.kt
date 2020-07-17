@file:Suppress("Unused")

package blue.starry.stella.worker.platform

import blue.starry.jsonkt.JsonObject
import blue.starry.jsonkt.delegation.*

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

    data class Bookmark(override val json: JsonObject): JsonModel {
        val illusts by modelList { Illust(it) }
        val nextUrl by nullableString("next_url")
    }

    data class Illust(override val json: JsonObject): JsonModel {
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
