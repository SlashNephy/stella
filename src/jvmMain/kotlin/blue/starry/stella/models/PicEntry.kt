package blue.starry.stella.models

import blue.starry.stella.models.internal.KindSerializer
import blue.starry.stella.models.internal.MediaExtensionSerializer
import blue.starry.stella.models.internal.PlatformSerializer
import blue.starry.stella.models.internal.SensitiveLevelSerializer
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.litote.kmongo.Id

/**
 * 登録されたメディアのエントリーを表すデータクラス
 */
@Serializable
data class PicEntry(
    /**
     * ID
     */
    @Contextual
    val _id: Id<PicEntry>,

    /**
     * 作品名
     */
    val title: String,

    /**
     * 作品説明
     */
    val description: String,

    /**
     * 作品のソース URL
     */
    val url: String,

    /**
     * エントリーに付けられたタグのリスト
     */
    val tags: List<Tag>,

    /**
     * エントリーを追加したユーザー
     */
    @Deprecated("Do not use this property anymore.")
    val user: String? = null,

    /**
     * 作品の掲載元
     */
    val platform: Platform,

    /**
     * 作品の年齢制限
     */
    val sensitive_level: SensitiveLevel,

    /**
     * 作品の種別
     */
    val kind: Kind = Kind.Illust,

    /**
     * エントリーのタイムスタンプの情報
     */
    val timestamp: Timestamp,

    /**
     * 作品の作者の情報
     */
    val author: Author,

    /**
     * 作品のメディアの情報
     */
    val media: List<Media>,

    /**
     * エントリーの評価
     */
    val rating: Rating,

    /**
     * 作品の人気度
     */
    val popularity: Popularity
) {
    /**
     * エントリーに付けられたタグを表すデータクラス
     */
    @Serializable
    data class Tag(
        /**
         * タグ
         */
        val value: String,

        /**
         * タグを登録したユーザー (IP アドレス)
         */
        val user: String?,

        /**
         * タグがロックされており, 編集不可能であるか?
         *
         * 作品の掲載元に存在するタグはロックされている
         */
        val locked: Boolean
    )

    /**
     * エントリーのタイムスタンプの情報を表すデータクラス
     */
    @Serializable
    data class Timestamp(
        /**
         * 作品の投稿日時 (エポックミリ秒)
         *
         * メディアが差し替えられたなどの理由により更新される場合がある
         */
        val created: Long,

        /**
         * エントリーの追加日時 (エポックミリ秒)
         *
         * 最初にエントリーが Stella に追加された日時を表す
         */
        val added: Long,

        /**
         * エントリーがユーザーにより更新された日時 (エポックミリ秒)
         *
         * タグが変更された場合などに更新される
         */
        val manual_updated: Long?,

        /**
         * エントリーがシステムにより更新された日時 (エポックミリ秒)
         *
         * 自動リフレッシュなどにより更新される
         */
        val auto_updated: Long?,

        /**
         * 作品が掲載元で利用不可能であるか?
         *
         * 作品が掲載元から削除されている場合に有効になる
         */
        val archived: Boolean = false,
    )

    /**
     * 作品の作者の情報を表すデータクラス
     */
    @Serializable
    data class Author(
        /**
         * 作者名
         */
        val name: String,

        /**
         * 作者のユーザー名
         *
         * プラットフォームによってはユーザー名が存在しない場合がある
         */
        val username: String?,

        /**
         * 作者のプロフィール URL
         */
        val url: String,

        /**
         * 作者のユーザー ID
         *
         * プラットフォームによってはユーザー ID が存在しない場合がある
         */
        val id: String? = null
    )

    /**
     * 作品のメディアの情報を表すデータクラス
     */
    @Serializable
    data class Media(
        /**
         * メディアの 0 から始まるインデックス
         */
        val index: Int,

        /**
         * メディアのファイル名
         */
        val filename: String,

        /**
         * メディアの掲載元のオリジナル URL
         */
        val original: String,

        /**
         * メディアの拡張子
         */
        val ext: MediaExtension
    )

    /**
     * エントリーの評価を表すデータクラス
     */
    @Serializable
    data class Rating(
        /**
         * 評価数
         */
        val count: Int,

        /**
         * 評価値
         */
        val score: Int
    )

    /**
     * 作品の人気度を表すデータクラス
     */
    @Serializable
    data class Popularity(
        /**
         * いいね数
         *
         * プラットフォームにより, 存在しない場合や呼称が異なる場合がある
         */
        val like: Int?,

        /**
         * ブックマーク数
         *
         * プラットフォームにより, 存在しない場合や呼称が異なる場合がある
         */
        val bookmark: Int?,

        /**
         * 閲覧数
         *
         * プラットフォームにより, 存在しない場合や呼称が異なる場合がある
         */
        val view: Int?,

        /**
         * リツイート数
         *
         * プラットフォームにより, 存在しない場合や呼称が異なる場合がある
         */
        val retweet: Int?,

        /**
         * リプライ数
         *
         * プラットフォームにより, 存在しない場合や呼称が異なる場合がある
         */
        val reply: Int?
    )

    /**
     * 作品の掲載元を表す列挙型
     */
    @Serializable(PlatformSerializer::class)
    enum class Platform {
        /**
         * [Twitter](https://twitter.com)
         */
        Twitter,

        /**
         * [Pixiv](https://www.pixiv.net)
         */
        Pixiv,

        /**
         * [Nijie](https://nijie.info)
         */
        Nijie
    }

    /**
     * 作品の年齢制限を表す列挙型
     */
    @Serializable(SensitiveLevelSerializer::class)
    enum class SensitiveLevel {
        /**
         * 全年齢対象
         */
        Safe,

        /**
         * R-15
         */
        R15,

        /**
         * R-18
         */
        R18,

        /**
         * R-18G
         */
        R18G
    }

    /**
     * メディアの拡張子を表す列挙型
     */
    @Serializable(MediaExtensionSerializer::class)
    @Suppress("EnumEntryName")
    enum class MediaExtension {
        /**
         * JPEG 画像 (.jpg)
         */
        jpg,

        /**
         * JPEG 画像 (.jpeg)
         */
        jpeg,

        /**
         * PNG 画像 (.png)
         */
        png,

        /**
         * GIF 画像 (.gif)
         */
        gif,

        /**
         * MP4 動画 (.mp4)
         */
        mp4
    }

    /**
     * 作品の種別を表す列挙型
     */
    @Serializable(KindSerializer::class)
    @Suppress("unused")
    enum class Kind {
        /**
         * イラスト
         */
        Illust,

        /**
         * 写真
         */
        Photo
    }
}
