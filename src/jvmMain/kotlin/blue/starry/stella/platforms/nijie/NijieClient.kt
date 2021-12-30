package blue.starry.stella.platforms.nijie

import blue.starry.jsonkt.parseObject
import blue.starry.jsonkt.toJsonObject
import blue.starry.stella.Stella
import blue.starry.stella.platforms.nijie.models.Bookmark
import blue.starry.stella.platforms.nijie.models.Picture
import blue.starry.stella.platforms.nijie.models.PictureJson
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import org.jsoup.Jsoup
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class NijieClient(private val email: String, private val password: String) {
    private val mutex = Mutex()
    private var loggedIn = false

    private suspend fun isLoggedIn(): Boolean {
        return mutex.withLock {
            loggedIn
        }
    }

    private suspend fun login() {
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

        Stella.Http.submitForm<Unit>(parameters) {
            url("https://nijie.info/login_int.php")
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
        if (!isLoggedIn()) {
            login()
        }
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
        if (!isLoggedIn()) {
            login()
        }
        val html = Stella.Http.get<String>("https://nijie.info/bookmark_edit.php?id=$id") {
            setHeaders()
        }.let {
            Jsoup.parse(it)
        }
        val key = html.select("input[value=$id]").last()!!.attr("name")
        val parameters = Parameters.build {
            append(key, id)
        }

        Stella.Http.submitForm<Unit>(parameters) {
            url("https://nijie.info/bookmark_delete.php")
            setHeaders()
            expectSuccess = false
        }
    }

    private val formatter = DateTimeFormatter.ofPattern("EEE MMM d HH:mm:ss yyyy", Locale.ENGLISH)
    suspend fun picture(id: String): Picture {
        if (!isLoggedIn()) {
            login()
        }
        val jsoup = Stella.Http.get<String>("https://nijie.info/view.php?id=$id") {
            setHeaders()
        }.let {
            Jsoup.parse(it)
        }
        val jsoup2 = Stella.Http.get<String>("https://nijie.info/view_popup.php?id=$id") {
            setHeaders()
        }.let {
            Jsoup.parse(it)
        }

        val json = jsoup.select("script[type=application/ld+json]").first()!!.html().parseObject { PictureJson(it) }
        val tags = jsoup.select("li[class=tag]").map { it.select("span[class=tag_name]").text() }
        val images = jsoup2.select("img[class=box-shadow999]").map { it.attr("src") }.map { "https:$it" }
        val like = jsoup.getElementById("good_cnt")!!.text().toInt()
        val bookmark = jsoup.getElementById("nuita_cnt")!!.text().toInt()
        val reply = jsoup.getElementById("comment_list_js")!!.childNodeSize() / 2
        val view = viewCount(id)

        return Picture(
            json.name, json.author.name, json.author.sameAs, LocalDateTime.parse(json.datePublished, formatter).atZone(
                ZoneId.of("UTC")
            ).toInstant().toEpochMilli(), images, json.description, "https://nijie.info/view.php?id=$id", id, tags, like, bookmark, reply, view
        )
    }

    private suspend fun viewCount(id: String): Int {
        if (!isLoggedIn()) {
            login()
        }

        val parameters = Parameters.build {
            append("id", id)
        }

        return runCatching {
            Stella.Http.submitForm<String>(parameters) {
                url("https://nijie.info/php/ajax/add_view_count.php")
                header(HttpHeaders.Referrer, "https://nijie.info/view.php?id=$id")
                header("X-Requested-With", "XMLHttpRequest")
                setHeaders()
            }.toJsonObject()["view_count"]!!.jsonPrimitive.int
        }.getOrDefault(0)
    }

    suspend fun download(url: String, file: File) {
        val response = Stella.Http.get<ByteArray>(url) {
            setHeaders()
            header(HttpHeaders.Referrer, "https://nijie.info/")
        }

        file.writeBytes(response)
    }

    private fun HttpRequestBuilder.setHeaders() {
        header(HttpHeaders.AcceptLanguage, "ja")
        userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.80 Safari/537.36")
    }
}
