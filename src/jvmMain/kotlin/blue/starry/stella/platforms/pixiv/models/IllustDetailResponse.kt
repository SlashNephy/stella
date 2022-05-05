package blue.starry.stella.platforms.pixiv.models

import blue.starry.stella.platforms.pixiv.entities.Illust
import kotlinx.serialization.Serializable

@Serializable
data class IllustDetailResponse(val illust: Illust)
