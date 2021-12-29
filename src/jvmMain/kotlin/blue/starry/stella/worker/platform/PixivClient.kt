package blue.starry.stella.worker.platform

import blue.starry.jsonkt.parseObject
import blue.starry.stella.Env
import blue.starry.stella.logger
import blue.starry.stella.worker.StellaCookies
import blue.starry.stella.worker.StellaHttpClient
import io.ktor.client.features.cookies.addCookie
import io.ktor.client.request.*
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.forms.submitForm
import io.ktor.http.Cookie
import io.ktor.http.HttpHeaders
import io.ktor.http.Parameters
import io.ktor.http.userAgent
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.nio.file.Path
import java.security.MessageDigest
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.io.path.writeBytes

class PixivClient(private val refreshToken: String) {
    private val mutex = Mutex()

    private var token: PixivModel.Token? = null
    private suspend fun isLoggedIn(): Boolean {
        return mutex.withLock {
            token != null
        }
    }

    private suspend fun login() {
        if (isLoggedIn()) {
            return
        }

        if (Env.PIXIV_PHPSESSIONID != null) {
            val cookie = Cookie(
                name = "PHPSESSID",
                value = Env.PIXIV_PHPSESSIONID!!,
                domain = ".pixiv.net",
                path = "/",
                httpOnly = true,
                secure = true,
            )
            StellaCookies.addCookie("https://www.pixiv.net", cookie)
        }

        // Refresh session
        StellaHttpClient.get<Unit>("https://www.pixiv.net") {
            setBrowserHeaders(null)
        }

        token = mutex.withLock {
            StellaHttpClient.post<String> {
                url("https://oauth.secure.pixiv.net/auth/token")
                userAgent("PixivAndroidApp/5.0.234 (Android 11; Pixel 5)")

                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssxxx")
                val time = OffsetDateTime.now(ZoneId.of("UTC")).format(formatter)
                header("X-Client-Time", time)

                header("X-Client-Hash", MessageDigest.getInstance("MD5").digest(
                    (time + "28c1fdd170a5204386cb1313c7077b34f83e4aaf4aa829ce78c231e05b0bae2c").toByteArray()
                ).joinToString("") {
                    "%02x".format(it)
                })

                val parameters = Parameters.build {
                    append("get_secure_url", "1")
                    append("client_id", "MOBrBDS8blbauoSck0ZfDbtuzpyT")
                    append("client_secret", "lsACyCD94FhDUtGTXi3QzcFE2uU1hqtDaKeqrdwj")
                    append("grant_type", "refresh_token")
                    append("refresh_token", token?.response?.refreshToken ?: refreshToken)
                }
                body = FormDataContent(parameters)
            }.parseObject {
                logger.trace { it }

                PixivModel.Token(it)
            }
        }

        logger.info { "Pixiv にログインしました。" }
    }

    suspend fun logout() {
        mutex.withLock {
            token = null
        }
    }

    suspend fun getBookmarks(private: Boolean): PixivModel.Bookmarks {
        login()

        return StellaHttpClient.get<String>("https://app-api.pixiv.net/v1/user/bookmarks/illust") {
            parameter("user_id", token?.response?.user?.id)
            parameter("restrict", if (private) "private" else "public")

            setHeaders()
        }.parseObject {
            PixivModel.Bookmarks(it)
        }
    }

    suspend fun deleteBookmark(id: Int) {
        login()

        val parameters = Parameters.build {
            append("illust_id", id.toString())
        }

        StellaHttpClient.submitForm<Unit>("https://app-api.pixiv.net/v1/illust/bookmark/delete", parameters) {
            setHeaders()
        }
    }

    suspend fun getArtworkPage(id: Int) {
        StellaHttpClient.get<Unit>("https://www.pixiv.net/artworks/$id") {
            setBrowserHeaders(null)
        }
    }

    private suspend inline fun <reified T> callAjax(url: String, referer: String?): T {
        val response = StellaHttpClient.get<PixivModel.AjaxResponse<T>>(url) {
            setBrowserHeaders(referer)
        }

        if (response.error) {
            error(response.message)
        }

        return response.body
    }

    suspend fun getIllust(id: Int) = callAjax<PixivModel.Illust>("https://www.pixiv.net/ajax/illust/$id", "https://www.pixiv.net/artworks/$id")
    suspend fun getIllustUgoiraMeta(id: String) = callAjax<PixivModel.IllustUgoiraMeta>("https://www.pixiv.net/ajax/illust/$id/ugoira_meta?lang=ja", "https://www.pixiv.net/artworks/$id")

    suspend fun download(url: String, path: Path) {
        val response = StellaHttpClient.get<ByteArray>(url) {
            setHeaders(false)
            header(HttpHeaders.Origin, "https://www.pixiv.net")
            header(HttpHeaders.Referrer, "https://www.pixiv.net/")
        }

        path.writeBytes(response)
    }

    private fun HttpRequestBuilder.setHeaders(requireAuth: Boolean = true) {
        header("App-OS", "ios")
        header("App-OS-Version", "12.2")
        header("App-Version", "7.6.2")
        userAgent("PixivIOSApp/7.6.2 (iOS 12.2; iPhone9,1)")

        if (requireAuth) {
            val token = token?.response?.accessToken ?: error("Login required.")
            header(HttpHeaders.Authorization, "Bearer $token")
        }
    }

    private fun HttpRequestBuilder.setBrowserHeaders(referer: String?) {
        header(HttpHeaders.AcceptLanguage, "ja,und;q=0.9")
        header(HttpHeaders.CacheControl, "no-cache")
        header(HttpHeaders.Pragma, "no-cache")
        header(HttpHeaders.Accept, "application/json")
        userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4692.56 Safari/537.36")

        header("Authority", "${url.protocol.name}://${url.host}")
        header(HttpHeaders.Origin, "${url.protocol.name}://${url.host}")
        header(HttpHeaders.Referrer, referer ?: "${url.protocol.name}://${url.host}/")
    }
}
