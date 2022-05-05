package blue.starry.stella.models

import kotlinx.serialization.Serializable

/**
 * エントリーのタグ情報を表すデータクラス
 */
@Serializable
data class PicTags(
    /**
     * エントリーに付けられたタグのリスト
     */
    val tags: List<String>
)
