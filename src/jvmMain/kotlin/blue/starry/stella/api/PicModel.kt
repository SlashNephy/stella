@file:Suppress("Unused")

package blue.starry.stella.api

import blue.starry.jsonkt.JsonObject
import blue.starry.jsonkt.delegation.*

data class PicModel(override val json: JsonObject): JsonModel {
    val title by string
    val description by string
    val url by string
    val tags by modelList { Tag(it) }
    val user by nullableString

    val platform by string
    val sensitiveLevel by int("sensitive_level")

    val timestamp by model { Timestamp(it) }
    val author by model { Author(it) }
    val media by modelList { Media(it) }

    val rating by model { Rating(it) }
    val popularity by model { Popularity(it) }

    data class Tag(override val json: JsonObject): JsonModel {
        val value by string
        val user by nullableString
        val locked by boolean
    }

    data class Timestamp(override val json: JsonObject): JsonModel {
        val created by long
        val added by long
        val manualUpdated by long("manual_updated")
        val autoUpdated by long("auto_updated")
    }

    data class Author(override val json: JsonObject): JsonModel {
        val name by string
        val username by nullableString
        val url by string
    }

    data class Media(override val json: JsonObject): JsonModel {
        val index by int
        val filename by string
        val original by string("original")
        val ext by string
    }

    data class Rating(override val json: JsonObject): JsonModel {
        val count by int
        val score by int
    }

    data class Popularity(override val json: JsonObject): JsonModel {
        val like by nullableInt
        val bookmark by nullableInt
        val view by nullableInt
        val retweet by nullableInt
        val reply by nullableInt
    }
}
