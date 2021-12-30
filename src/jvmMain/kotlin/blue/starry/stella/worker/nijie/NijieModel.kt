@file:Suppress("Unused")

package blue.starry.stella.worker.nijie

import blue.starry.jsonkt.JsonObject
import blue.starry.jsonkt.delegation.JsonModel
import blue.starry.jsonkt.delegation.int
import blue.starry.jsonkt.delegation.model
import blue.starry.jsonkt.delegation.string

object NijieModel {
    data class Bookmark(val title: String, val id: String)

    data class Picture(val title: String, val authorName: String, val authorUrl: String, val createdAt: Long, val media: List<String>, val description: String, val url: String, val id: String, val tags: List<String>, val like: Int, val bookmark: Int, val reply: Int, val view: Int)

    data class PictureJson(override val json: JsonObject): JsonModel {
        val context by string("@context")
        val type by string("@type")
        val author by model { Author(it) }
        val contentLocation by string
        val copyrightHolder by model { CopyrightHolder(it) }
        val copyrightYear by int
        val creator by model { Creator(it) }
        val dateModified by string
        val datePublished by string
        val description by string
        val editor by model { Editor(it) }
        val genre by string
        val height by int
        val interactionCount by string
        val name by string
        val text by string
        val thumbnailUrl by string
        val uploadDate by string
        val width by int

        data class Author(override val json: JsonObject): JsonModel {
            val type by string("@type")
            val description by string
            val image by string
            val name by string
            val sameAs by string
        }

        data class CopyrightHolder(override val json: JsonObject): JsonModel {
            val type by string("@type")
            val description by string
            val image by string
            val name by string
            val sameAs by string
        }

        data class Creator(override val json: JsonObject): JsonModel {
            val type by string("@type")
            val description by string
            val image by string
            val name by string
            val sameAs by string
        }

        data class Editor(override val json: JsonObject): JsonModel {
            val type by string("@type")
            val description by string
            val image by string
            val name by string
            val sameAs by string
        }
    }
}
