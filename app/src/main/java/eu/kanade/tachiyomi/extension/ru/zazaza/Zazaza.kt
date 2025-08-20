//Comment
package eu.kanade.tachiyomi.extension.ru.zazaza
//Comment
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.ParsedHttpSource
import okhttp3.Headers
import okhttp3.Request
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class Zazaza : ParsedHttpSource() {

    override val name = "Zazaza"
    override val baseUrl = "https://a.zazaza.me"
    override val lang = "ru"
    override val supportsLatest = true

    // Базовые заголовки, без хелперов
    override fun headers(): Headers =
        Headers.Builder()
            .add("User-Agent", "Mozilla/5.0 (Android) TachiyomiSY")
            .add("Referer", "$baseUrl/")
            .build()

    // ==== POPULAR ====
    override fun popularMangaRequest(page: Int): Request = GET("$baseUrl/", headers())
    override fun popularMangaSelector(): String = "a" // заглушка
    override fun popularMangaFromElement(element: Element): SManga =
        SManga.create().apply {
            val a = element.selectFirst("a[href]")!!
            title = a.text().ifBlank { "Title" }
            setUrlWithoutDomain(a.attr("href"))
            thumbnail_url = null
        }
    override fun popularMangaNextPageSelector(): String? = null

    // ==== LATEST (пока как popular) ====
    override fun latestUpdatesRequest(page: Int): Request = popularMangaRequest(page)
    override fun latestUpdatesSelector(): String = popularMangaSelector()
    override fun latestUpdatesFromElement(element: Element): SManga = popularMangaFromElement(element)
    override fun latestUpdatesNextPageSelector(): String? = null

    // ==== SEARCH (пока как popular) ====
    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request =
        popularMangaRequest(page)
    override fun searchMangaSelector(): String = popularMangaSelector()
    override fun searchMangaFromElement(element: Element): SManga = popularMangaFromElement(element)
    override fun searchMangaNextPageSelector(): String? = null

    // ==== DETAILS ====
    override fun mangaDetailsParse(document: Document): SManga =
        SManga.create().apply {
            title = document.selectFirst("h1, .title")?.text().orEmpty()
            description = document.selectFirst(".description, .summary")?.text()
            author = null
            artist = null
            genre = null
            status = SManga.UNKNOWN
            thumbnail_url = document.selectFirst(".cover img, .poster img")?.absUrl("src")
        }

    // ==== CHAPTERS ====
    override fun chapterListSelector(): String = "a" // заглушка
    override fun chapterFromElement(element: Element): SChapter =
        SChapter.create().apply {
            val a = element.selectFirst("a[href]")!!
            name = a.text().ifBlank { "Chapter" }
            setUrlWithoutDomain(a.attr("href"))
            date_upload = 0L
        }

    // ==== PAGES ====
    override fun pageListParse(document: Document): List<Page> {
        // Заглушка: если на странице есть <img>, вернём их — иначе пусто
        val imgs = document.select("img[src]")
        return imgs.mapIndexed { i, el -> Page(i, document.location(), el.absUrl("src")) }
    }

    override fun imageUrlParse(document: Document): String {
        throw UnsupportedOperationException("Not used")
    }
}