package blue.starry.stella.models

import kotlinx.serialization.Serializable

/**
 * タグの置換ルールを表すデータクラス
 */
@Serializable
data class PicTagReplace(
    /**
     * タグの変換元
     */
    val from: String,

    /**
     * タグの変換先
     */
    val to: String
)
