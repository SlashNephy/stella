package blue.starry.stella.models

import kotlinx.serialization.Serializable

/**
 * 登録されているエントリーの要約を表すデータクラス
 */
@Serializable
data class PicSummary(
    /**
     * エントリー登録数
     */
    val entries: Long,

    /**
     * メディア登録数
     */
    val media: Long
)
