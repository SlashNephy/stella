package blue.starry.stella.platforms.pixiv

import blue.starry.stella.Stella
import blue.starry.stella.platforms.pixiv.models.BookmarksResponse
import blue.starry.stella.platforms.pixiv.models.IllustDetailResponse
import blue.starry.stella.platforms.pixiv.models.Token
import blue.starry.stella.platforms.pixiv.models.UgoiraMetadataResponse
import io.ktor.client.request.*
import io.ktor.client.request.forms.FormDataContent
import io.ktor.http.HttpHeaders
import io.ktor.http.Parameters
import io.ktor.http.userAgent
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.security.MessageDigest
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class PixivClient(private val refreshToken: String) {
    private val mutex = Mutex()

    private var token: Token? = null
    private suspend fun isLoggedIn(): Boolean {
        return mutex.withLock {
            token != null
        }
    }

    private suspend fun login() {
        if (isLoggedIn()) {
            return
        }

        token = mutex.withLock {
            Stella.Http.post<Token> {
                url("https://oauth.secure.pixiv.net/auth/token")
                setAppHeaders(requireAuth = false)
                val parameters = Parameters.build {
                    append("get_secure_url", "1")
                    append("client_id", "MOBrBDS8blbauoSck0ZfDbtuzpyT")
                    append("client_secret", "lsACyCD94FhDUtGTXi3QzcFE2uU1hqtDaKeqrdwj")
                    append("grant_type", "refresh_token")
                    append("refresh_token", token?.response?.refreshToken ?: refreshToken)
                }
                body = FormDataContent(parameters)
            }
        }

        Stella.Logger.info { "Pixiv にログインしました。" }
    }

    suspend fun logout() {
        mutex.withLock {
            token = null
        }
    }

    suspend fun getBookmarks(private: Boolean): BookmarksResponse {
        login()

        return Stella.Http.get("https://app-api.pixiv.net/v1/user/bookmarks/illust") {
            parameter("user_id", token?.response?.user?.id)
            parameter("restrict", if (private) "private" else "public")

            setAppHeaders()
        }
    }

    suspend fun deleteBookmark(id: Int) {
        login()

        val form = Parameters.build {
            append("illust_id", id.toString())
        }

        Stella.Http.post<Unit>("https://app-api.pixiv.net/v1/illust/bookmark/delete") {
            body = FormDataContent(form)
            setAppHeaders()
        }
    }

    suspend fun addFollow(id: Int, private: Boolean) {
        login()

        Stella.Http.post<Unit>("https://app-api.pixiv.net/v1/user/follow/add") {
            parameter("user_id", id)
            parameter("restrict", if (private) "private" else "public")

        }
    }

    suspend fun getIllustDetail(id: Int): IllustDetailResponse {
        login()

        return Stella.Http.get("https://app-api.pixiv.net/v1/illust/detail") {
            parameter("filter", "for_android")
            parameter("illust_id", id)
            setAppHeaders()
        }
    }

    suspend fun getUgoiraMetadata(id: Int): UgoiraMetadataResponse {
        login()

        return Stella.Http.get("https://app-api.pixiv.net/v1/ugoira/metadata") {
            parameter("illust_id", id)
            setAppHeaders()
        }
    }

    suspend fun download(url: String): ByteArray {
        return Stella.Http.get(url) {
            userAgent("PixivAndroidApp/6.34.0 (Android 12; Pixel 6 Pro)")
            header(HttpHeaders.Referrer, "https://app-api.pixiv.net/")
        }
    }

    private fun HttpRequestBuilder.setAppHeaders(requireAuth: Boolean = true) {
        if (requireAuth) {
            val token = token?.response?.accessToken ?: error("Login required.")
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        header(HttpHeaders.AcceptLanguage, "ja_JP")
        userAgent("PixivAndroidApp/6.34.0 (Android 12; Pixel 6 Pro)")

        header("app-accept-language", "ja")
        header("App-OS", "android")
        header("App-OS-Version", "12")
        header("App-Version", "6.34.0")

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssxxx")
        val time = OffsetDateTime.now(ZoneId.of("UTC")).format(formatter)
        header("X-Client-Time", time)

        val md5 = MessageDigest.getInstance("MD5")
        val hash = md5.digest(
            (time + "28c1fdd170a5204386cb1313c7077b34f83e4aaf4aa829ce78c231e05b0bae2c").toByteArray()
        ).joinToString("") {
            "%02x".format(it)
        }
        header("X-Client-Hash", hash)
    }
}
