package blue.starry.stella.worker.platform

import blue.starry.jsonkt.parseObject
import blue.starry.stella.logger
import blue.starry.stella.worker.StellaHttpClient
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.security.MessageDigest
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.*

class PixivClient(private val email: String, private val password: String) {
    private val lock = Mutex()

    private var token: PixivModel.Token? = null
    private val isLoggedIn: Boolean
        get() = token != null

    private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ENGLISH)
    private fun HttpRequestBuilder.setHeaders(credentials: Boolean) {
        DateTimeFormatter.ISO_DATE_TIME
        val time = dateFormat.format(Instant.now())

        userAgent("PixivAndroidApp/5.0.64 (Android 6.0)")
        header("X-Client-Time", time)
        header("X-Client-Hash", MessageDigest.getInstance("MD5").digest(
            (time + "28c1fdd170a5204386cb1313c7077b34f83e4aaf4aa829ce78c231e05b0bae2c").toByteArray()
        ).joinToString("") {
            "%02x".format(it)
        })

        if (credentials) {
            requireNotNull(token)
            header(HttpHeaders.Authorization, "Bearer ${token?.response?.accessToken}")
        }
    }

    private suspend fun login() {
        if (isLoggedIn) {
            return
        }

        val parameters = Parameters.build {
            append("grant_type", "password")
            append("client_id", "MOBrBDS8blbauoSck0ZfDbtuzpyT")
            append("username", email)
            append("get_secure_url", "1")
            append("password", password)
            append("client_secret", "lsACyCD94FhDUtGTXi3QzcFE2uU1hqtDaKeqrdwj")
        }

        token = lock.withLock(token) {
            StellaHttpClient.submitForm<String>(parameters) {
                url("https://oauth.secure.pixiv.net/auth/token")

                setHeaders(false)
            }.parseObject {
                PixivModel.Token(it)
            }
        }

        logger.info { "Pixiv にログインしました。" }
    }

    suspend fun logout() {
        lock.withLock {
            token = null
        }
    }

    suspend fun getBookmarks(private: Boolean): PixivModel.Bookmark {
        login()

        return StellaHttpClient.get<String>("https://app-api.pixiv.net/v1/user/bookmarks/illust") {
            parameter("user_id", token?.response?.user?.id)
            parameter("restrict", if (private) "private" else "public")

            setHeaders(true)
        }.parseObject {
            PixivModel.Bookmark(it)
        }
    }

    suspend fun addBookmark(id: Int, private: Boolean) {
        login()

        val parameters = Parameters.build {
            append("restrict", if (private) "private" else "public")
            append("illust_id", id.toString())
        }

        return StellaHttpClient.submitForm("https://app-api.pixiv.net/v2/illust/bookmark/add", parameters) {
            setHeaders(true)
        }
    }

    suspend fun deleteBookmark(id: Int) {
        login()

        val parameters = Parameters.build {
            append("illust_id", id.toString())
        }

        return StellaHttpClient.submitForm("https://app-api.pixiv.net/v1/illust/bookmark/delete", parameters) {
            setHeaders(true)
        }
    }

    suspend fun download(url: String, file: File) {
        val response = StellaHttpClient.get<ByteArray>(url) {
            setHeaders(false)
            header(HttpHeaders.Referrer, "https://app-api.pixiv.net/")
        }

        file.writeBytes(response)
    }
}

