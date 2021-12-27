package blue.starry.stella.worker.platform

import blue.starry.jsonkt.parseObject
import blue.starry.stella.logger
import blue.starry.stella.worker.StellaHttpClient
import io.ktor.client.request.*
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.forms.submitForm
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

        token = mutex.withLock {
            StellaHttpClient.submitForm<String> {
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

    suspend fun addBookmark(id: Int, private: Boolean) {
        login()

        val parameters = Parameters.build {
            append("restrict", if (private) "private" else "public")
            append("illust_id", id.toString())
        }

        StellaHttpClient.submitForm<Unit>("https://app-api.pixiv.net/v2/illust/bookmark/add", parameters) {
            setHeaders()
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

    private suspend fun <T> callAjax(url: String): T {
        val response = StellaHttpClient.get<PixivModel.AjaxResponse<T>>(url) {
            setBrowserHeaders()
        }

        if (response.error) {
            error(response.message)
        }

        return response.body
    }

    suspend fun getIllust(id: Int) = callAjax<PixivModel.Illust>("https://www.pixiv.net/ajax/illust/$id")
    suspend fun getIllustPages(id: String) = callAjax<List<PixivModel.IllustPage>>("https://www.pixiv.net/ajax/illust/$id/pages?lang=ja")
    suspend fun getIllustUgoiraMeta(id: String) = callAjax<PixivModel.IllustUgoiraMeta>("https://www.pixiv.net/ajax/illust/$id/ugoira_meta")

    suspend fun download(url: String, path: Path) {
        val response = StellaHttpClient.get<ByteArray>(url) {
            setHeaders(false)
            header(HttpHeaders.Referrer, "https://app-api.pixiv.net/")
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

    private fun HttpRequestBuilder.setBrowserHeaders() {
        header("Accept", "*/*")
        header("Accept-Language", "ja-JP")
        header("User-Agent", "PixivIOSApp/7.6.2 (iOS 12.2; iPhone9,1)")
    }
}

