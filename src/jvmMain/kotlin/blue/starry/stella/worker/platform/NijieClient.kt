package blue.starry.stella.worker.platform

import blue.starry.jsonkt.parseObject
import blue.starry.jsonkt.toJsonObject
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.cookies.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.int
import org.apache.commons.lang3.time.FastDateFormat
import org.jsoup.Jsoup
import java.io.File
import java.util.*

object NijieClient {
    private val httpClient = HttpClient(CIO) {
        install(HttpCookies)
    }
    var isLoggedIn = checkSession()
        private set

    private fun checkSession(): Boolean {
        return runCatching {
            runBlocking {
                httpClient.get<HttpStatement>("https://nijie.info") {
                    setHeaders()
                }.execute {
                    it.call.request.url.encodedPath == "/"
                }
            }
        }.getOrNull() ?: false
    }

    private fun HttpRequestBuilder.setHeaders() {
        header(HttpHeaders.AcceptLanguage, "ja")
        userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.80 Safari/537.36")
    }

    suspend fun login(email: String, password: String) {
        val parameters = Parameters.build {
            append("email", email)
            append("password", password)
            append("save", "on")

            val jsoup = httpClient.get<String>("https://nijie.info/login.php") {
                setHeaders()
            }.let {
                Jsoup.parse(it)
            }
            append("ticket", jsoup.select("input[name=ticket]").attr("value"))
            append("url", jsoup.select("input[name=url]").attr("value"))
        }

        httpClient.submitForm<HttpStatement>(parameters) {
            url("https://nijie.info/login_int.php")
            setHeaders()
            header(HttpHeaders.Referrer, "https://nijie.info/login.php")
        }.execute()

        isLoggedIn = true
    }

    suspend fun bookmarks(page: Int = 1): List<NijieModel.Bookmark> {
        val html = httpClient.get<String>("https://nijie.info/okiniiri.php?p=$page") {
            setHeaders()
        }

        return Jsoup.parse(html).select("div[class=nijie-bookmark]").map { element ->
            val title = element.select("p[class=title]").text()
            val id = element.select("p[class=nijiedao]").first().getElementsByTag("a").first().attr("href").split("=").last()

            NijieModel.Bookmark(title, id)
        }
    }

    suspend fun deleteBookmark(id: String) {
        val jsoup = httpClient.get<String>("https://nijie.info/bookmark_edit.php?id=$id") {
            setHeaders()
        }.let {
            Jsoup.parse(it)
        }

        val key = jsoup.select("input[value=$id]").last().attr("name")

        val parameters = Parameters.build {
            append(key, id)
        }

        httpClient.submitForm<HttpStatement>(parameters) {
            url("https://nijie.info/bookmark_delete.php")
            setHeaders()
        }.execute()
    }

    private val format = FastDateFormat.getInstance("EEE MMM d HH:mm:ss yyyy", Locale.ENGLISH)
    suspend fun picture(id: String): NijieModel.Picture {
        val jsoup = httpClient.get<String>("https://nijie.info/view.php?id=$id") {
            setHeaders()
        }.let {
            Jsoup.parse(it)
        }
        val jsoup2 = httpClient.get<String>("https://nijie.info/view_popup.php?id=$id") {
            setHeaders()
        }.let {
            Jsoup.parse(it)
        }

        val json = jsoup.select("script[type=application/ld+json]").first().html().parseObject { NijieModel.PictureJson(it) }
        val tags = jsoup.select("li[class=tag]").map { it.select("span[class=tag_name]").text() }
        val images = jsoup2.select("img[class=box-shadow999]").map { it.attr("src") }.map { "https:$it" }
        val like = jsoup.getElementById("good_cnt").text().toInt()
        val bookmark = jsoup.getElementById("nuita_cnt").text().toInt()
        val reply = jsoup.getElementById("comment_list_js").childNodeSize() / 2
        val view = viewCount(id)

        return NijieModel.Picture(json.name, json.author.name, json.author.sameAs, format.parse(json.datePublished).time, images, json.description, "https://nijie.info/view.php?id=$id", id, tags, like, bookmark, reply, view)
    }

    private suspend fun viewCount(id: String): Int {
        val parameters = Parameters.build {
            append("id", id)
        }

        return runCatching {
            httpClient.submitForm<String>(parameters) {
                url("https://nijie.info/php/ajax/add_view_count.php")
                header(HttpHeaders.Referrer, "https://nijie.info/view.php?id=$id")
                header("X-Requested-With", "XMLHttpRequest")
                setHeaders()
            }.toJsonObject()["view_count"]!!.int
        }.getOrNull() ?: 0
    }

    suspend fun download(url: String, file: File) {
        val response = httpClient.get<ByteArray>(url) {
            setHeaders()
            header(HttpHeaders.Referrer, "https://nijie.info/")
        }

        file.outputStream().use {
            it.write(response)
        }
    }
}
