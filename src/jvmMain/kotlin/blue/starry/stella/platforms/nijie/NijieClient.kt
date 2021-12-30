package blue.starry.stella.platforms.nijie

import blue.starry.stella.Stella
import blue.starry.stella.platforms.nijie.entities.Illust
import blue.starry.stella.platforms.nijie.models.Bookmark
import blue.starry.stella.platforms.nijie.models.IllustMeta
import blue.starry.stella.platforms.nijie.models.ViewCount
import io.ktor.client.features.expectSuccess
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.http.HttpHeaders
import io.ktor.http.Parameters
import io.ktor.http.userAgent
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.jsoup.Jsoup

class NijieClient(private val email: String, private val password: String) {
    private val mutex = Mutex()
    private var loggedIn = false

    private suspend fun isLoggedIn(): Boolean {
        return mutex.withLock {
            loggedIn
        }
    }

    private suspend fun login() {
        if (isLoggedIn()) {
            return
        }

        Stella.Http.get<Unit>("https://nijie.info") {
            setHeaders()
            expectSuccess = false
        }

        val parameters = Parameters.build {
            append("email", email)
            append("password", password)
            append("save", "on")

            val jsoup = Stella.Http.get<String>("https://nijie.info/age_jump.php?url=") {
                setHeaders()
                expectSuccess = false
            }.let {
                Jsoup.parse(it)
            }

            append("ticket", jsoup.select("input[name=ticket]").attr("value"))
            append("url", jsoup.select("input[name=url]").attr("value"))
        }

        Stella.Http.post<Unit>("https://nijie.info/login_int.php") {
            body = FormDataContent(parameters)

            setHeaders()
            header(HttpHeaders.Referrer, "https://nijie.info/login.php?url=$url")
            expectSuccess = false
        }

        loggedIn = true
        Stella.Logger.info { "Nijie にログインしました。" }
    }

    suspend fun logout() {
        mutex.withLock {
            loggedIn = false
        }
    }

    suspend fun bookmarks(page: Int = 1): List<Bookmark> {
        login()

        val html = Stella.Http.get<String>("https://nijie.info/okiniiri.php?p=$page") {
            setHeaders()
        }

        return Jsoup.parse(html).select("div[class=nijie-bookmark]").map { element ->
            val title = element.select("p[class=title]").text()
            val id = element.select("p[class=nijiedao]").first()!!.getElementsByTag("a").first()!!.attr("href").split("=").last()

            Bookmark(title, id)
        }
    }

    suspend fun deleteBookmark(id: String) {
        login()

        val parameters = Parameters.build {
            val html = Stella.Http.get<String>("https://nijie.info/bookmark_edit.php?id=$id") {
                setHeaders()
            }.let {
                Jsoup.parse(it)
            }

            val key = html.select("input[value=$id]").last()!!.attr("name")
            append(key, id)
        }

        Stella.Http.post<Unit>("https://nijie.info/bookmark_delete.php") {
            body = FormDataContent(parameters)

            setHeaders()
            expectSuccess = false
        }
    }

    suspend fun getIllustMeta(id: String): IllustMeta {
        login()

        val jsoup = Stella.Http.get<String>("https://nijie.info/view.php?id=$id") {
            setHeaders()
        }.let {
            Jsoup.parse(it)
        }
        val script = jsoup.select("script[type=application/ld+json]").first()!!.html()
        val json = Json.decodeFromString<Illust>(script)

        val tags = jsoup.select("li[class=tag]").map {
            it.select("span[class=tag_name]").text()
        }

        val like = jsoup.getElementById("good_cnt")!!.text().toInt()
        val bookmark = jsoup.getElementById("nuita_cnt")!!.text().toInt()
        val reply = jsoup.getElementById("comment_list_js")!!.childNodeSize() / 2

        val jsoup2 = Stella.Http.get<String>("https://nijie.info/view_popup.php?id=$id") {
            setHeaders()
        }.let {
            Jsoup.parse(it)
        }
        val mediaUrls = jsoup2.select("img[class=box-shadow999]").map { it.attr("src") }.map { "https:$it" }
        val view = getViewCount(id)

        return IllustMeta(
            illust = json,
            mediaUrls = mediaUrls,
            url = "https://nijie.info/view.php?id=$id",
            id = id,
            tags = tags,
            like = like,
            bookmark = bookmark,
            reply = reply,
            view = view.viewCount
        )
    }

    private suspend fun getViewCount(id: String): ViewCount {
        login()

        val parameters = Parameters.build {
            append("id", id)
        }

        return Stella.Http.post("https://nijie.info/php/ajax/add_view_count.php") {
            body = FormDataContent(parameters)

            setHeaders()
            header(HttpHeaders.Referrer, "https://nijie.info/view.php?id=$id")
            header("X-Requested-With", "XMLHttpRequest")
        }
    }

    suspend fun download(url: String): ByteArray {
        return Stella.Http.get(url) {
            setHeaders()
        }
    }

    private fun HttpRequestBuilder.setHeaders() {
        header(HttpHeaders.AcceptLanguage, "ja")
        header(HttpHeaders.Referrer, "https://nijie.info/")
        userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.80 Safari/537.36")
    }
}
