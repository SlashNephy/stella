package blue.starry.stella.platforms.pixiv.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Token(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("expires_in")
    val expiresIn: Int,
    @SerialName("refresh_token")
    val refreshToken: String,
    val response: Response,
    val scope: String,
    @SerialName("token_type")
    val tokenType: String,
    val user: User
) {
    @Serializable
    data class Response(
        @SerialName("access_token")
        val accessToken: String,
        @SerialName("expires_in")
        val expiresIn: Int,
        @SerialName("refresh_token")
        val refreshToken: String,
        val scope: String,
        @SerialName("token_type")
        val tokenType: String,
        val user: User
    ) {
        @Serializable
        data class User(
            val account: String,
            val id: String,
            @SerialName("is_mail_authorized")
            val isMailAuthorized: Boolean,
            @SerialName("is_premium")
            val isPremium: Boolean,
            @SerialName("mail_address")
            val mailAddress: String,
            val name: String,
            @SerialName("profile_image_urls")
            val profileImageUrls: ProfileImageUrls,
            @SerialName("x_restrict")
            val xRestrict: Int
        ) {
            @Serializable
            data class ProfileImageUrls(
                @SerialName("px_16x16")
                val px16x16: String,
                @SerialName("px_170x170")
                val px170x170: String,
                @SerialName("px_50x50")
                val px50x50: String
            )
        }
    }

    @Serializable
    data class User(
        val account: String,
        val id: String,
        @SerialName("is_mail_authorized")
        val isMailAuthorized: Boolean,
        @SerialName("is_premium")
        val isPremium: Boolean,
        @SerialName("mail_address")
        val mailAddress: String,
        val name: String,
        @SerialName("profile_image_urls")
        val profileImageUrls: ProfileImageUrls,
        @SerialName("x_restrict")
        val xRestrict: Int
    ) {
        @Serializable
        data class ProfileImageUrls(
            @SerialName("px_16x16")
            val px16x16: String,
            @SerialName("px_170x170")
            val px170x170: String,
            @SerialName("px_50x50")
            val px50x50: String
        )
    }
}
