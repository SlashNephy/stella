package blue.starry.stella.platforms.pixiv.models

import blue.starry.jsonkt.JsonObject
import blue.starry.jsonkt.delegation.JsonModel
import blue.starry.jsonkt.delegation.int
import blue.starry.jsonkt.delegation.model
import blue.starry.jsonkt.delegation.string

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
        }
    }
}
