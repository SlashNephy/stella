@file:Suppress("Unused")

package blue.starry.stella.api

import blue.starry.jsonkt.JsonObject
import blue.starry.jsonkt.delegation.JsonModel
import blue.starry.jsonkt.delegation.string

data class TagReplaceTableModel(override val json: JsonObject): JsonModel {
    val from by string
    val to by string
}
