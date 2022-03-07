package blue.starry.stella.models

import kotlinx.serialization.Serializable

/**
 * API サーバーで発生したエラーを表すデータクラス
 */
@Serializable
data class ApiError(
    /**
     * エラーコード
     */
    val code: Int,

    /**
     * エラーメッセージ
     */
    val message: String
)
